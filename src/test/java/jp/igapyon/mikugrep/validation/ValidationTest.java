package jp.igapyon.mikugrep.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikugrep.contract.RequestContract;
import jp.igapyon.mikugrep.model.EncodingRuleInput;
import jp.igapyon.mikugrep.model.IgnoreMode;
import jp.igapyon.mikugrep.model.OutputMode;
import jp.igapyon.mikugrep.model.QueryType;
import jp.igapyon.mikugrep.model.SearchTarget;
import jp.igapyon.mikugrep.model.SupportedEncoding;

class ValidationTest {
    @Test
    void expandsEffectiveRequestDefaultsWithStableValues() throws Exception {
        ValidationResult result = Validation.validateAndNormalize("{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"}"
                + "}");

        assertTrue(result.ok);
        assertEquals(".", result.effectiveRequest.root);
        assertEquals(QueryType.LITERAL, result.effectiveRequest.query.type);
        assertEquals("RepositoryMap", result.effectiveRequest.query.text);
        assertEquals(Arrays.asList(SearchTarget.CONTENT), result.effectiveRequest.search.targets);
        assertEquals(Boolean.TRUE, result.effectiveRequest.search.recursive);
        assertEquals(Integer.valueOf(20), result.effectiveRequest.search.maxDepth);
        assertEquals(Long.valueOf(10485760L), result.effectiveRequest.search.maxFileBytes);
        assertEquals(Integer.valueOf(1000000), result.effectiveRequest.search.maxLineChars);
        assertEquals(Integer.valueOf(100000), result.effectiveRequest.search.maxFilesVisited);
        assertEquals(Integer.valueOf(10000), result.effectiveRequest.search.maxDirectoriesVisited);
        assertEquals(0, result.effectiveRequest.search.includeFileNamePatterns.size());
        assertTrue(result.effectiveRequest.search.excludeFileNamePatterns.containsAll(RequestContract.DEFAULT_EXCLUDE_FILES));
        assertTrue(result.effectiveRequest.search.excludeDirNamePatterns.containsAll(RequestContract.DEFAULT_EXCLUDE_DIRS));
        assertEquals(OutputMode.SUMMARY, result.effectiveRequest.output.mode);
        assertEquals(Integer.valueOf(200), result.effectiveRequest.output.maxMatches);
        assertEquals(Integer.valueOf(20), result.effectiveRequest.output.maxMatchesPerFile);
        assertEquals(Integer.valueOf(240), result.effectiveRequest.output.maxLineLength);
        assertEquals(Integer.valueOf(3), result.effectiveRequest.output.maxSnippetsPerFile);
        assertEquals(Integer.valueOf(0), result.effectiveRequest.output.contextLinesBefore);
        assertEquals(Integer.valueOf(0), result.effectiveRequest.output.contextLinesAfter);
        assertEquals(SupportedEncoding.UTF_8, result.effectiveRequest.encoding.defaultEncoding);
        assertEquals(null, result.effectiveRequest.encoding.preset);
        assertEquals(0, result.effectiveRequest.encoding.rules.size());
        assertEquals("skip", result.effectiveRequest.encoding.onDecodeError);
        assertEquals(IgnoreMode.AUTO, result.effectiveRequest.ignore.mode);
        assertEquals(Arrays.asList(".gitignore", ".ignore", ".git/info/exclude"), result.effectiveRequest.ignore.sources);
        assertEquals(Boolean.FALSE, result.effectiveRequest.ignore.useGlobalGitignore);
        assertEquals(0, result.effectiveRequest.ignore.loadedSources.size());
    }

    @Test
    void replacesDefaultExcludesAndOverridesRecursiveDepth() throws Exception {
        ValidationResult result = Validation.validateAndNormalize("{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"},"
                + "\"search\":{"
                + "\"recursive\":false,"
                + "\"includeFileNamePatterns\":[\"*.java\"],"
                + "\"excludeFileNamePatterns\":[\"README.md\"],"
                + "\"excludeDirNamePatterns\":[\"skip\"]"
                + "}"
                + "}");

        assertTrue(result.ok);
        assertEquals(Integer.valueOf(0), result.effectiveRequest.search.maxDepth);
        assertEquals(Arrays.asList("*.java"), result.effectiveRequest.search.includeFileNamePatterns);
        assertEquals(Arrays.asList("README.md"), result.effectiveRequest.search.excludeFileNamePatterns);
        assertEquals(Arrays.asList("skip"), result.effectiveRequest.search.excludeDirNamePatterns);
    }

    @Test
    void usesRequestExcludePatternsAsReplacementForDefaultExcludes() throws Exception {
        ValidationResult fileResult = Validation.validateAndNormalize(baseRequestWith("\"search\":{\"excludeFileNamePatterns\":[]}"));
        ValidationResult dirResult = Validation.validateAndNormalize(baseRequestWith("\"search\":{\"excludeDirNamePatterns\":[]}"));

        assertTrue(fileResult.ok);
        assertEquals(0, fileResult.effectiveRequest.search.excludeFileNamePatterns.size());
        assertTrue(dirResult.ok);
        assertEquals(0, dirResult.effectiveRequest.search.excludeDirNamePatterns.size());
    }

    @Test
    void rejectsUnknownFields() throws Exception {
        ValidationResult result = Validation.validateAndNormalize("{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"},"
                + "\"typo\":true"
                + "}");

        assertInvalid(result, "unknown_field");
        assertEquals("unknown field: typo", result.message);
    }

    @Test
    void rejectsInvalidRequestShapes() throws Exception {
        assertInvalid(Validation.validateAndNormalize("null"), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":[]")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":[]")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":[]")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"ignore\":[]")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"includeFileNamePatterns\":[\"*.txt\",1]}")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"excludeFileNamePatterns\":null}")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"excludeDirNamePatterns\":null}")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"rules\":[{\"fileNamePattern\":\"*.txt\",\"encoding\":\"utf-8\",\"extra\":true}]}")), "unknown_field");
    }

    @Test
    void rejectsInvalidRegexAndLimits() throws Exception {
        assertInvalid(Validation.validateAndNormalize(baseRequest("regex", "[")), "invalid_regex");
        assertInvalid(Validation.validateAndNormalize(baseRequest("regex", repeat("a", 1001))), "regex_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequest("regex", "^(a+)+$")), "unsafe_regex");
        assertTrue(Validation.validateAndNormalize(baseRequest("regex", "^([a+])+$")).ok);

        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"maxMatches\":10001}")), "max_matches_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"maxDepth\":51}")), "max_depth_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"maxLineChars\":10000001}")), "max_line_chars_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"maxFilesVisited\":1000001}")), "max_files_visited_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"maxDirectoriesVisited\":100001}")), "max_directories_visited_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"maxFileBytes\":104857601}")), "max_file_bytes_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"maxLineLength\":4001}")), "max_line_length_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"maxSnippetsPerFile\":101}")), "max_snippets_per_file_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"mode\":\"detail\",\"contextLines\":21}")), "context_lines_too_large");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"mode\":\"summary\",\"contextLines\":1}")), "invalid_context_lines");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"mode\":\"detail\",\"contextLines\":1,\"contextLinesBefore\":1}")), "invalid_context_lines");
    }

    @Test
    void rejectsInvalidTopLevelValues() throws Exception {
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"version\":2")), "invalid_version");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"query\":{\"type\":\"glob\",\"text\":\"RepositoryMap\"}")), "invalid_search_target");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"query\":{\"type\":\"literal\",\"text\":\"\"}")), "empty_query");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"targets\":[]}")), "invalid_search_targets");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"targets\":[\"path\"]}")), "invalid_search_target");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"search\":{\"targets\":[\"content\",\"content\"]}")), "duplicate_search_target");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"output\":{\"mode\":\"raw\"}")), "invalid_output_mode");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"default\":\"euc-jp\"}")), "invalid_encoding");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"onDecodeError\":\"replace\"}")), "invalid_request");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"ignore\":{\"mode\":\"bad\"}")), "invalid_ignore_mode");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"ignore\":{\"sources\":[\"bad\"]}")), "invalid_ignore_source");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"ignore\":{\"sources\":[\".gitignore\",\".gitignore\"]}")), "invalid_ignore_sources");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"ignore\":{\"useGlobalGitignore\":true}")), "invalid_ignore_global");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"ignore\":{\"mode\":\"none\",\"sources\":[\".gitignore\"]}")), "invalid_ignore_sources");
    }

    @Test
    void validatesEncodingRules() throws Exception {
        ValidationResult result = Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"rules\":["
                + "{\"pathPattern\":\"src/legacy.txt\",\"encoding\":\"shift_jis\"},"
                + "{\"fileNamePattern\":\"plain.txt\",\"encoding\":\"utf-8\"}"
                + "]}"));

        assertTrue(result.ok);
        EncodingRuleInput first = result.effectiveRequest.encoding.rules.get(0);
        EncodingRuleInput second = result.effectiveRequest.encoding.rules.get(1);
        assertEquals("src/legacy.txt", first.pathPattern);
        assertEquals(SupportedEncoding.SHIFT_JIS, first.encoding);
        assertEquals("plain.txt", second.fileNamePattern);
        assertEquals(SupportedEncoding.UTF_8, second.encoding);

        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"rules\":\"bad\"}")), "invalid_encoding_rule");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"rules\":[{\"fileNamePattern\":\"*.txt\",\"encoding\":\"euc-jp\"}]}")), "invalid_encoding_rule");
        assertInvalid(Validation.validateAndNormalize(baseRequestWith("\"encoding\":{\"rules\":[{\"encoding\":\"utf-8\"}]}")), "invalid_encoding_rule");
    }

    private static void assertInvalid(ValidationResult result, String code) {
        assertFalse(result.ok);
        assertEquals(code, result.code);
    }

    private static String baseRequestWith(String extraField) {
        return "{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"},"
                + extraField
                + "}";
    }

    private static String baseRequest(String type, String text) {
        return "{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"" + type + "\",\"text\":\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
                + "}";
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
