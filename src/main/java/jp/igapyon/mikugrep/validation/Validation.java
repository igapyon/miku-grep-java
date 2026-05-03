package jp.igapyon.mikugrep.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikugrep.contract.RequestContract;
import jp.igapyon.mikugrep.contract.RequestFieldShape;
import jp.igapyon.mikugrep.json.MikuGrepJson;
import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.EncodingOptions;
import jp.igapyon.mikugrep.model.EncodingRuleInput;
import jp.igapyon.mikugrep.model.IgnoreMode;
import jp.igapyon.mikugrep.model.IgnoreOptions;
import jp.igapyon.mikugrep.model.OutputMode;
import jp.igapyon.mikugrep.model.OutputOptions;
import jp.igapyon.mikugrep.model.Query;
import jp.igapyon.mikugrep.model.QueryType;
import jp.igapyon.mikugrep.model.SearchOptions;
import jp.igapyon.mikugrep.model.SearchTarget;
import jp.igapyon.mikugrep.model.SupportedEncoding;
import jp.igapyon.mikugrep.regex.RegexSafety;

public final class Validation {
    private static final RequestFieldShape ENCODING_RULE_SHAPE = new RequestFieldShape()
            .field("pathPattern")
            .field("fileNamePattern")
            .field("encoding");

    private Validation() {
    }

    public static ValidationResult validateAndNormalize(String json) throws IOException {
        return validateAndNormalize(MikuGrepJson.readTree(json));
    }

    public static ValidationResult validateAndNormalize(JsonNode request) {
        if (!isPlainObject(request)) {
            return invalid("invalid_request", "request must be an object");
        }

        String unknown = RequestContract.REQUEST_SHAPE.findUnknownField(request);
        if (unknown != null) {
            return invalid("unknown_field", "unknown field: " + unknown);
        }
        if (!intValueEquals(request.get("version"), RequestContract.VERSION)) {
            return invalid("invalid_version", "version must be 1");
        }
        if (!isNonEmptyText(request.get("root"))) {
            return invalid("invalid_request", "root must be a non-empty string");
        }
        if (!isPlainObject(request.get("query"))) {
            return invalid("invalid_request", "query must be an object");
        }

        JsonNode queryInput = request.get("query");
        String queryTypeText = textValue(queryInput.get("type"));
        QueryType queryType = queryType(queryTypeText);
        if (queryType == null) {
            return invalid("invalid_query_type", "query.type must be literal or regex");
        }
        String queryText = textValue(queryInput.get("text"));
        if (queryText == null) {
            return invalid("invalid_request", "query.text must be a string");
        }
        if (queryText.length() == 0) {
            return invalid("empty_query", "query.text must not be empty");
        }
        if (queryType == QueryType.REGEX) {
            ValidationResult regexResult = validateRegex(queryText);
            if (regexResult != null) {
                return regexResult;
            }
        }

        JsonNode searchInput = objectOrEmpty(request.get("search"));
        JsonNode outputInput = objectOrEmpty(request.get("output"));
        JsonNode encodingInput = objectOrEmpty(request.get("encoding"));
        JsonNode ignoreInput = objectOrEmpty(request.get("ignore"));
        if (searchInput == null || outputInput == null || encodingInput == null || ignoreInput == null) {
            return invalid("invalid_request", "search, output, encoding, and ignore must be objects when specified");
        }

        SearchBuildResult searchResult = buildSearch(searchInput);
        if (!searchResult.result.ok) {
            return searchResult.result;
        }
        OutputBuildResult outputResult = buildOutput(outputInput);
        if (!outputResult.result.ok) {
            return outputResult.result;
        }
        EncodingBuildResult encodingResult = buildEncoding(encodingInput);
        if (!encodingResult.result.ok) {
            return encodingResult.result;
        }
        IgnoreBuildResult ignoreResult = buildIgnore(ignoreInput);
        if (!ignoreResult.result.ok) {
            return ignoreResult.result;
        }

        EffectiveRequest effectiveRequest = new EffectiveRequest();
        effectiveRequest.root = request.get("root").textValue();
        effectiveRequest.query = new Query();
        effectiveRequest.query.type = queryType;
        effectiveRequest.query.text = queryText;
        effectiveRequest.search = searchResult.search;
        effectiveRequest.output = outputResult.output;
        effectiveRequest.encoding = encodingResult.encoding;
        effectiveRequest.ignore = ignoreResult.ignore;
        return ValidationResult.ok(effectiveRequest);
    }

    private static SearchBuildResult buildSearch(JsonNode input) {
        SearchOptions defaults = RequestContract.DEFAULTS.search();
        SearchOptions search = new SearchOptions();

        List<SearchTarget> targets = input.has("targets") ? searchTargets(input.get("targets")) : defaults.targets;
        if (targets == null || targets.isEmpty()) {
            return SearchBuildResult.invalid("invalid_search_targets", "search.targets must be a non-empty array of filepath, directory, or content");
        }
        for (SearchTarget target : targets) {
            if (target == null) {
                return SearchBuildResult.invalid("invalid_search_target", "search.targets[] must be filepath, directory, or content");
            }
        }
        if (hasDuplicateTargets(targets)) {
            return SearchBuildResult.invalid("duplicate_search_target", "search.targets must not contain duplicate values");
        }

        JsonNode recursiveNode = input.get("recursive");
        if (recursiveNode == null || recursiveNode.isNull()) {
            search.recursive = defaults.recursive;
        } else if (recursiveNode.isBoolean()) {
            search.recursive = Boolean.valueOf(recursiveNode.booleanValue());
        } else {
            return SearchBuildResult.invalid("invalid_request", "search.recursive must be boolean");
        }

        Long maxDepth = integerOrDefault(input.get("maxDepth"), Long.valueOf(defaults.maxDepth.longValue()));
        Long maxFileBytes = integerOrDefault(input.get("maxFileBytes"), defaults.maxFileBytes);
        Long maxLineChars = integerOrDefault(input.get("maxLineChars"), Long.valueOf(defaults.maxLineChars.longValue()));
        Long maxFilesVisited = integerOrDefault(input.get("maxFilesVisited"), Long.valueOf(defaults.maxFilesVisited.longValue()));
        Long maxDirectoriesVisited = integerOrDefault(input.get("maxDirectoriesVisited"), Long.valueOf(defaults.maxDirectoriesVisited.longValue()));

        ValidationResult limit = validateIntegerLimit(maxDepth, "search.maxDepth", 0, RequestContract.LIMITS.maxDepth, "max_depth_too_large");
        if (limit != null) {
            return SearchBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxFileBytes, "search.maxFileBytes", 0, RequestContract.LIMITS.maxFileBytes, "max_file_bytes_too_large");
        if (limit != null) {
            return SearchBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxLineChars, "search.maxLineChars", 1, RequestContract.LIMITS.maxLineChars, "max_line_chars_too_large");
        if (limit != null) {
            return SearchBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxFilesVisited, "search.maxFilesVisited", 1, RequestContract.LIMITS.maxFilesVisited, "max_files_visited_too_large");
        if (limit != null) {
            return SearchBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxDirectoriesVisited, "search.maxDirectoriesVisited", 1, RequestContract.LIMITS.maxDirectoriesVisited, "max_directories_visited_too_large");
        if (limit != null) {
            return SearchBuildResult.invalid(limit);
        }

        List<String> includeFileNamePatterns = stringArrayOrEmpty(input.get("includeFileNamePatterns"));
        List<String> excludeFileNamePatterns = input.has("excludeFileNamePatterns")
                ? stringArray(input.get("excludeFileNamePatterns"))
                : RequestContract.DEFAULT_EXCLUDE_FILES;
        List<String> excludeDirNamePatterns = input.has("excludeDirNamePatterns")
                ? stringArray(input.get("excludeDirNamePatterns"))
                : RequestContract.DEFAULT_EXCLUDE_DIRS;
        if (includeFileNamePatterns == null) {
            return SearchBuildResult.invalid("invalid_request", "search.includeFileNamePatterns must be an array of strings");
        }
        if (excludeFileNamePatterns == null) {
            return SearchBuildResult.invalid("invalid_request", "search.excludeFileNamePatterns must be an array of strings");
        }
        if (excludeDirNamePatterns == null) {
            return SearchBuildResult.invalid("invalid_request", "search.excludeDirNamePatterns must be an array of strings");
        }

        search.targets = targets;
        search.maxDepth = Integer.valueOf(search.recursive.booleanValue() ? maxDepth.intValue() : 0);
        search.maxFileBytes = maxFileBytes;
        search.maxLineChars = Integer.valueOf(maxLineChars.intValue());
        search.maxFilesVisited = Integer.valueOf(maxFilesVisited.intValue());
        search.maxDirectoriesVisited = Integer.valueOf(maxDirectoriesVisited.intValue());
        search.includeFileNamePatterns = includeFileNamePatterns;
        search.excludeFileNamePatterns = excludeFileNamePatterns;
        search.excludeDirNamePatterns = excludeDirNamePatterns;
        return SearchBuildResult.ok(search);
    }

    private static OutputBuildResult buildOutput(JsonNode input) {
        OutputOptions defaults = RequestContract.DEFAULTS.output();
        OutputOptions output = new OutputOptions();

        String modeText = textValue(input.get("mode"));
        output.mode = modeText == null ? defaults.mode : outputMode(modeText);
        if (output.mode == null) {
            return OutputBuildResult.invalid("invalid_output_mode", "output.mode must be detail or summary");
        }

        boolean hasContextLines = input.has("contextLines");
        boolean hasContextLinesBefore = input.has("contextLinesBefore");
        boolean hasContextLinesAfter = input.has("contextLinesAfter");
        if (output.mode == OutputMode.SUMMARY && (hasContextLines || hasContextLinesBefore || hasContextLinesAfter)) {
            return OutputBuildResult.invalid("invalid_context_lines", "context lines are only supported in detail mode");
        }
        if (hasContextLines && (hasContextLinesBefore || hasContextLinesAfter)) {
            return OutputBuildResult.invalid("invalid_context_lines", "output.contextLines cannot be combined with contextLinesBefore or contextLinesAfter");
        }

        Long maxMatches = integerOrDefault(input.get("maxMatches"), Long.valueOf(defaults.maxMatches.longValue()));
        Long maxMatchesPerFile = integerOrDefault(input.get("maxMatchesPerFile"), Long.valueOf(defaults.maxMatchesPerFile.longValue()));
        Long maxLineLength = integerOrDefault(input.get("maxLineLength"), Long.valueOf(defaults.maxLineLength.longValue()));
        Long maxSnippetsPerFile = integerOrDefault(input.get("maxSnippetsPerFile"), Long.valueOf(defaults.maxSnippetsPerFile.longValue()));
        Long contextLinesBefore = hasContextLines
                ? integerOrDefault(input.get("contextLines"), null)
                : integerOrDefault(input.get("contextLinesBefore"), Long.valueOf(defaults.contextLinesBefore.longValue()));
        Long contextLinesAfter = hasContextLines
                ? integerOrDefault(input.get("contextLines"), null)
                : integerOrDefault(input.get("contextLinesAfter"), Long.valueOf(defaults.contextLinesAfter.longValue()));

        ValidationResult limit = validateIntegerLimit(maxMatches, "output.maxMatches", 1, RequestContract.LIMITS.maxMatches, "max_matches_too_large");
        if (limit != null) {
            return OutputBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxMatchesPerFile, "output.maxMatchesPerFile", 1, RequestContract.LIMITS.maxMatchesPerFile, "max_matches_per_file_too_large");
        if (limit != null) {
            return OutputBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxLineLength, "output.maxLineLength", 1, RequestContract.LIMITS.maxLineLength, "max_line_length_too_large");
        if (limit != null) {
            return OutputBuildResult.invalid(limit);
        }
        limit = validateIntegerLimit(maxSnippetsPerFile, "output.maxSnippetsPerFile", 1, RequestContract.LIMITS.maxSnippetsPerFile, "max_snippets_per_file_too_large");
        if (limit != null) {
            return OutputBuildResult.invalid(limit);
        }
        limit = validateContextLineLimit(contextLinesBefore, hasContextLines ? "output.contextLines" : "output.contextLinesBefore");
        if (limit != null) {
            return OutputBuildResult.invalid(limit);
        }
        limit = validateContextLineLimit(contextLinesAfter, hasContextLines ? "output.contextLines" : "output.contextLinesAfter");
        if (limit != null) {
            return OutputBuildResult.invalid(limit);
        }

        output.maxMatches = Integer.valueOf(maxMatches.intValue());
        output.maxMatchesPerFile = Integer.valueOf(maxMatchesPerFile.intValue());
        output.maxLineLength = Integer.valueOf(maxLineLength.intValue());
        output.maxSnippetsPerFile = Integer.valueOf(maxSnippetsPerFile.intValue());
        output.contextLinesBefore = Integer.valueOf(contextLinesBefore.intValue());
        output.contextLinesAfter = Integer.valueOf(contextLinesAfter.intValue());
        return OutputBuildResult.ok(output);
    }

    private static EncodingBuildResult buildEncoding(JsonNode input) {
        EncodingOptions defaults = RequestContract.DEFAULTS.encoding();
        EncodingOptions encoding = new EncodingOptions();

        String defaultText = textValue(input.get("default"));
        encoding.defaultEncoding = defaultText == null ? defaults.defaultEncoding : supportedEncoding(defaultText);
        if (encoding.defaultEncoding == null) {
            return EncodingBuildResult.invalid("invalid_encoding", "encoding.default must be utf-8 or shift_jis");
        }

        JsonNode onDecodeError = input.get("onDecodeError");
        encoding.onDecodeError = onDecodeError == null || onDecodeError.isNull() ? defaults.onDecodeError : textValue(onDecodeError);
        if (!"skip".equals(encoding.onDecodeError)) {
            return EncodingBuildResult.invalid("invalid_request", "encoding.onDecodeError must be skip");
        }

        JsonNode rulesNode = input.get("rules");
        if (rulesNode == null || rulesNode.isNull()) {
            encoding.rules = Collections.emptyList();
            return EncodingBuildResult.ok(encoding);
        }
        if (!rulesNode.isArray()) {
            return EncodingBuildResult.invalid("invalid_encoding_rule", "encoding.rules must be an array");
        }
        List<EncodingRuleInput> rules = new ArrayList<EncodingRuleInput>();
        for (JsonNode ruleNode : rulesNode) {
            if (!isPlainObject(ruleNode)) {
                return EncodingBuildResult.invalid("invalid_encoding_rule", "encoding rule is invalid");
            }
            String ruleUnknown = ENCODING_RULE_SHAPE.findUnknownField(ruleNode);
            if (ruleUnknown != null) {
                return EncodingBuildResult.invalid("unknown_field", "unknown field: encoding.rules[]." + ruleUnknown);
            }

            String pathPattern = optionalText(ruleNode, "pathPattern");
            String fileNamePattern = optionalText(ruleNode, "fileNamePattern");
            SupportedEncoding ruleEncoding = supportedEncoding(textValue(ruleNode.get("encoding")));
            if (ruleEncoding == null || (!truthy(pathPattern) && !truthy(fileNamePattern))) {
                return EncodingBuildResult.invalid("invalid_encoding_rule", "encoding rule is invalid");
            }
            if (ruleNode.has("pathPattern") && pathPattern == null) {
                return EncodingBuildResult.invalid("invalid_encoding_rule", "encoding rule is invalid");
            }
            if (ruleNode.has("fileNamePattern") && fileNamePattern == null) {
                return EncodingBuildResult.invalid("invalid_encoding_rule", "encoding rule is invalid");
            }

            EncodingRuleInput rule = new EncodingRuleInput();
            rule.pathPattern = pathPattern;
            rule.fileNamePattern = fileNamePattern;
            rule.encoding = ruleEncoding;
            rules.add(rule);
        }
        encoding.rules = rules;
        return EncodingBuildResult.ok(encoding);
    }

    private static IgnoreBuildResult buildIgnore(JsonNode input) {
        IgnoreOptions defaults = RequestContract.DEFAULTS.ignore();
        IgnoreOptions ignore = new IgnoreOptions();

        String modeText = textValue(input.get("mode"));
        ignore.mode = modeText == null ? defaults.mode : ignoreMode(modeText);
        if (ignore.mode == null) {
            return IgnoreBuildResult.invalid("invalid_ignore_mode", "ignore.mode must be auto or none");
        }
        if (ignore.mode == IgnoreMode.NONE && (input.has("sources") || input.has("useGlobalGitignore"))) {
            return IgnoreBuildResult.invalid("invalid_ignore_sources", "ignore.sources and ignore.useGlobalGitignore cannot be specified when ignore.mode is none");
        }

        List<String> sources = input.has("sources") ? stringArray(input.get("sources")) : defaults.sources;
        if (sources == null) {
            return IgnoreBuildResult.invalid("invalid_ignore_sources", "ignore.sources must be an array of strings");
        }
        for (String source : sources) {
            if (!isIgnoreSource(source)) {
                return IgnoreBuildResult.invalid("invalid_ignore_source", "ignore.sources[] must be .gitignore, .ignore, or .git/info/exclude");
            }
        }
        if (hasDuplicateStrings(sources)) {
            return IgnoreBuildResult.invalid("invalid_ignore_sources", "ignore.sources must not contain duplicate values");
        }

        JsonNode useGlobalGitignoreNode = input.get("useGlobalGitignore");
        if (useGlobalGitignoreNode == null || useGlobalGitignoreNode.isNull()) {
            ignore.useGlobalGitignore = defaults.useGlobalGitignore;
        } else if (useGlobalGitignoreNode.isBoolean()) {
            ignore.useGlobalGitignore = Boolean.valueOf(useGlobalGitignoreNode.booleanValue());
        } else {
            return IgnoreBuildResult.invalid("invalid_ignore_global", "ignore.useGlobalGitignore must be false");
        }
        if (ignore.useGlobalGitignore.booleanValue()) {
            return IgnoreBuildResult.invalid("invalid_ignore_global", "ignore.useGlobalGitignore must be false");
        }

        ignore.sources = ignore.mode == IgnoreMode.NONE ? Collections.<String>emptyList() : sources;
        ignore.loadedSources = new ArrayList<jp.igapyon.mikugrep.model.IgnoreLoadedSource>();
        return IgnoreBuildResult.ok(ignore);
    }

    private static ValidationResult validateRegex(String queryText) {
        if (queryText.length() > RequestContract.LIMITS.regexPatternLength) {
            return invalid("regex_too_large", "query.text regex pattern is too large");
        }
        try {
            Pattern.compile(queryText);
        } catch (PatternSyntaxException ex) {
            return invalid("invalid_regex", "query.text is not a valid regular expression");
        }
        if (RegexSafety.hasNestedQuantifiedGroup(queryText)) {
            return invalid("unsafe_regex", "query.text regex pattern has nested quantified groups");
        }
        return null;
    }

    private static ValidationResult validateIntegerLimit(Long value, String fieldPath, int minimum, long maximum, String tooLargeCode) {
        if (value == null || value.longValue() < minimum) {
            String kind = minimum == 0 ? "a non-negative integer" : "a positive integer";
            return invalid("invalid_request", fieldPath + " must be " + kind);
        }
        if (value.longValue() > maximum) {
            return invalid(tooLargeCode, fieldPath + " is too large");
        }
        return null;
    }

    private static ValidationResult validateContextLineLimit(Long value, String fieldPath) {
        ValidationResult result = validateIntegerLimit(value, fieldPath, 0, RequestContract.LIMITS.contextLines, "context_lines_too_large");
        if (result != null && "invalid_request".equals(result.code)) {
            return invalid("invalid_context_lines", fieldPath + " must be a non-negative integer");
        }
        return result;
    }

    private static JsonNode objectOrEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return MikuGrepJson.mapper().createObjectNode();
        }
        return isPlainObject(node) ? node : null;
    }

    private static boolean isPlainObject(JsonNode node) {
        return node != null && node.isObject();
    }

    private static boolean intValueEquals(JsonNode node, int expected) {
        return node != null && node.isIntegralNumber() && node.canConvertToInt() && node.intValue() == expected;
    }

    private static boolean isNonEmptyText(JsonNode node) {
        return node != null && node.isTextual() && node.textValue().length() > 0;
    }

    private static String textValue(JsonNode node) {
        return node != null && node.isTextual() ? node.textValue() : null;
    }

    private static String optionalText(JsonNode object, String fieldName) {
        if (!object.has(fieldName)) {
            return null;
        }
        return textValue(object.get(fieldName));
    }

    private static Long integerOrDefault(JsonNode node, Long defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (!node.isIntegralNumber() || !node.canConvertToLong()) {
            return null;
        }
        return Long.valueOf(node.longValue());
    }

    private static List<String> stringArrayOrEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return Collections.emptyList();
        }
        return stringArray(node);
    }

    private static List<String> stringArray(JsonNode node) {
        if (!node.isArray()) {
            return null;
        }
        List<String> values = new ArrayList<String>();
        for (JsonNode item : node) {
            if (!item.isTextual()) {
                return null;
            }
            values.add(item.textValue());
        }
        return values;
    }

    private static boolean truthy(String value) {
        return value != null && value.length() > 0;
    }

    private static QueryType queryType(String value) {
        if ("literal".equals(value)) {
            return QueryType.LITERAL;
        }
        if ("regex".equals(value)) {
            return QueryType.REGEX;
        }
        return null;
    }

    private static List<SearchTarget> searchTargets(JsonNode node) {
        if (!node.isArray()) {
            return null;
        }
        List<SearchTarget> targets = new ArrayList<SearchTarget>();
        for (JsonNode item : node) {
            if (!item.isTextual()) {
                return null;
            }
            targets.add(searchTarget(item.textValue()));
        }
        return targets;
    }

    private static SearchTarget searchTarget(String value) {
        if ("content".equals(value)) {
            return SearchTarget.CONTENT;
        }
        if ("filepath".equals(value)) {
            return SearchTarget.FILEPATH;
        }
        if ("directory".equals(value)) {
            return SearchTarget.DIRECTORY;
        }
        return null;
    }

    private static OutputMode outputMode(String value) {
        if ("detail".equals(value)) {
            return OutputMode.DETAIL;
        }
        if ("summary".equals(value)) {
            return OutputMode.SUMMARY;
        }
        return null;
    }

    private static IgnoreMode ignoreMode(String value) {
        if ("auto".equals(value)) {
            return IgnoreMode.AUTO;
        }
        if ("none".equals(value)) {
            return IgnoreMode.NONE;
        }
        return null;
    }

    private static boolean isIgnoreSource(String value) {
        return ".gitignore".equals(value) || ".ignore".equals(value) || ".git/info/exclude".equals(value);
    }

    private static boolean hasDuplicateTargets(List<SearchTarget> targets) {
        return new java.util.HashSet<SearchTarget>(targets).size() != targets.size();
    }

    private static boolean hasDuplicateStrings(List<String> values) {
        return new java.util.HashSet<String>(values).size() != values.size();
    }

    private static SupportedEncoding supportedEncoding(String value) {
        if ("utf-8".equals(value)) {
            return SupportedEncoding.UTF_8;
        }
        if ("shift_jis".equals(value)) {
            return SupportedEncoding.SHIFT_JIS;
        }
        return null;
    }

    private static ValidationResult invalid(String code, String message) {
        return ValidationResult.invalid(code, message);
    }

    private static final class SearchBuildResult {
        final ValidationResult result;
        final SearchOptions search;

        private SearchBuildResult(ValidationResult result, SearchOptions search) {
            this.result = result;
            this.search = search;
        }

        static SearchBuildResult ok(SearchOptions search) {
            return new SearchBuildResult(ValidationResult.ok(null), search);
        }

        static SearchBuildResult invalid(String code, String message) {
            return invalid(ValidationResult.invalid(code, message));
        }

        static SearchBuildResult invalid(ValidationResult result) {
            return new SearchBuildResult(result, null);
        }
    }

    private static final class OutputBuildResult {
        final ValidationResult result;
        final OutputOptions output;

        private OutputBuildResult(ValidationResult result, OutputOptions output) {
            this.result = result;
            this.output = output;
        }

        static OutputBuildResult ok(OutputOptions output) {
            return new OutputBuildResult(ValidationResult.ok(null), output);
        }

        static OutputBuildResult invalid(String code, String message) {
            return invalid(ValidationResult.invalid(code, message));
        }

        static OutputBuildResult invalid(ValidationResult result) {
            return new OutputBuildResult(result, null);
        }
    }

    private static final class EncodingBuildResult {
        final ValidationResult result;
        final EncodingOptions encoding;

        private EncodingBuildResult(ValidationResult result, EncodingOptions encoding) {
            this.result = result;
            this.encoding = encoding;
        }

        static EncodingBuildResult ok(EncodingOptions encoding) {
            return new EncodingBuildResult(ValidationResult.ok(null), encoding);
        }

        static EncodingBuildResult invalid(String code, String message) {
            return new EncodingBuildResult(ValidationResult.invalid(code, message), null);
        }
    }

    private static final class IgnoreBuildResult {
        final ValidationResult result;
        final IgnoreOptions ignore;

        private IgnoreBuildResult(ValidationResult result, IgnoreOptions ignore) {
            this.result = result;
            this.ignore = ignore;
        }

        static IgnoreBuildResult ok(IgnoreOptions ignore) {
            return new IgnoreBuildResult(ValidationResult.ok(null), ignore);
        }

        static IgnoreBuildResult invalid(String code, String message) {
            return new IgnoreBuildResult(ValidationResult.invalid(code, message), null);
        }
    }
}
