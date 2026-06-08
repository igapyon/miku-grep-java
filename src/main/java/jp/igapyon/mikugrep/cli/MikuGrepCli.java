package jp.igapyon.mikugrep.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jp.igapyon.mikugrep.coreapi.MikuGrep;
import jp.igapyon.mikugrep.json.MikuGrepJson;
import jp.igapyon.mikugrep.model.AgentDirectoryMatch;
import jp.igapyon.mikugrep.model.AgentFileMatch;
import jp.igapyon.mikugrep.model.ContentMatch;
import jp.igapyon.mikugrep.model.ContextLine;
import jp.igapyon.mikugrep.model.DirectoryMatch;
import jp.igapyon.mikugrep.model.DirectorySummaryMatch;
import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.FileListEntry;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.FileSummarySnippet;
import jp.igapyon.mikugrep.model.FilepathMatch;
import jp.igapyon.mikugrep.model.MikuGrepResult;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.ReadRangeCandidate;

public final class MikuGrepCli {
    public static final String PRODUCT_VERSION = "0.10.0";

    private MikuGrepCli() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        return run(args, System.in, out, err);
    }

    public static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        if (args.length == 1 && "--version".equals(args[0])) {
            out.print("miku-grep " + PRODUCT_VERSION + "\n");
            return 0;
        }
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            out.print(HelpText.helpText());
            return 0;
        }
        if (args.length > 0) {
            ParsedArgs parsed = parseArgs(args);
            if (!parsed.ok) {
                err.print(parsed.message + "\n");
                err.print("usage: miku-grep QUERY [ROOT] [--agent|--files|--context N|--format json]\n");
                return 2;
            }
            try {
                MikuGrepResult result = MikuGrep.runRequest(parsed.request);
                if ("json".equals(parsed.format)) {
                    out.print(MikuGrepJson.writePrettyResult(result));
                    out.print("\n");
                } else if (result.ok) {
                    out.print(formatTextResult(result, parsed.textMode));
                } else {
                    err.print(formatTextError(result));
                }
                return result.ok ? 0 : 1;
            } catch (IOException ex) {
                err.print("unexpected runtime error: " + ex.toString() + "\n");
                return 3;
            } catch (RuntimeException ex) {
                err.print("unexpected runtime error: " + ex.toString() + "\n");
                return 3;
            }
        }
        try {
            JsonNode request = MikuGrepJson.readTree(readAll(in));
            MikuGrepResult result = MikuGrep.runRequest(request);
            out.print(MikuGrepJson.writePrettyResult(result));
            out.print("\n");
            return result.ok ? 0 : 1;
        } catch (IOException ex) {
            err.print("malformed stdin: " + ex.getMessage() + "\n");
            return 2;
        } catch (RuntimeException ex) {
            err.print("unexpected runtime error: " + ex.toString() + "\n");
            return 3;
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static ParsedArgs parseArgs(String[] args) {
        List<String> positionals = new ArrayList<String>();
        ObjectNode request = MikuGrepJson.mapper().createObjectNode();
        request.put("version", 1);
        request.put("root", ".");
        ObjectNode query = request.putObject("query");
        query.put("type", "literal");
        query.put("text", "");
        ObjectNode search = request.putObject("search");
        ArrayNode targets = search.putArray("targets");
        targets.add("content");
        ObjectNode output = request.putObject("output");
        output.put("mode", "summary");

        String format = "text";
        String textMode = "summary";
        boolean filesMode = false;
        int filesOptionIndex = -1;
        int firstPositionalIndex = -1;
        boolean agentMode = false;
        boolean contextMode = false;
        boolean topFilesMode = false;
        boolean regexMode = false;
        boolean globMode = false;
        boolean pathMode = false;
        boolean allTargetsMode = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--format".equals(arg)) {
                String value = valueAfter(args, ++i);
                if (!"text".equals(value) && !"json".equals(value)) {
                    return ParsedArgs.error("--format must be text or json");
                }
                format = value;
            } else if ("--json".equals(arg)) {
                format = "json";
            } else if ("--agent".equals(arg)) {
                output.put("mode", "agent");
                output.put("sort", "relevance");
                output.put("includeReadfileRequestHints", true);
                textMode = "agent";
                agentMode = true;
            } else if ("--files".equals(arg)) {
                filesMode = true;
                filesOptionIndex = i;
                textMode = "files";
            } else if ("--context".equals(arg)) {
                Integer value = parseInteger(valueAfter(args, ++i));
                if (value == null || value.intValue() < 0) {
                    return ParsedArgs.error("--context requires a non-negative integer");
                }
                output.put("mode", "detail");
                output.put("contextLines", value.intValue());
                contextMode = true;
            } else if ("--limit".equals(arg)) {
                Integer value = parseInteger(valueAfter(args, ++i));
                if (value == null || value.intValue() <= 0) {
                    return ParsedArgs.error("--limit requires a positive integer");
                }
                output.put("maxMatches", value.intValue());
            } else if ("--top-files".equals(arg)) {
                Integer value = parseInteger(valueAfter(args, ++i));
                if (value == null || value.intValue() <= 0) {
                    return ParsedArgs.error("--top-files requires a positive integer");
                }
                output.put("maxMatches", value.intValue());
                output.put("mode", "agent");
                output.put("sort", "relevance");
                textMode = "agent";
                topFilesMode = true;
            } else if ("--encoding".equals(arg)) {
                String value = valueAfter(args, ++i);
                if (!"utf-8".equals(value) && !"shift_jis".equals(value)) {
                    return ParsedArgs.error("--encoding must be utf-8 or shift_jis");
                }
                encoding(request).put("default", value);
            } else if ("--encoding-preset".equals(arg)) {
                String value = valueAfter(args, ++i);
                if (!"japanese-legacy".equals(value)) {
                    return ParsedArgs.error("--encoding-preset must be japanese-legacy");
                }
                encoding(request).put("preset", value);
            } else if ("--ignore-case".equals(arg) || "-i".equals(arg)) {
                ((ObjectNode) request.get("query")).put("case", "insensitive");
            } else if ("--regex".equals(arg)) {
                ((ObjectNode) request.get("query")).put("type", "regex");
                regexMode = true;
            } else if ("--glob".equals(arg)) {
                ((ObjectNode) request.get("query")).put("type", "glob");
                putTargets(search, "filepath");
                globMode = true;
            } else if ("--path".equals(arg)) {
                putTargets(search, "filepath");
                pathMode = true;
            } else if ("--all-targets".equals(arg)) {
                putTargets(search, "filepath", "directory", "content");
                allTargetsMode = true;
            } else if ("--detect-git-root".equals(arg)) {
                request.put("detectGitRoot", true);
            } else if ("--no-ignore".equals(arg)) {
                ObjectNode ignore = request.putObject("ignore");
                ignore.put("mode", "none");
            } else if (arg.startsWith("-")) {
                return ParsedArgs.error("unknown option: " + arg);
            } else {
                if (firstPositionalIndex < 0) {
                    firstPositionalIndex = i;
                }
                positionals.add(arg);
            }
        }

        int outputModes = (filesMode ? 1 : 0) + ((agentMode || topFilesMode) ? 1 : 0) + (contextMode ? 1 : 0);
        if (outputModes > 1) {
            return ParsedArgs.error("--files, --agent/--top-files, and --context are mutually exclusive");
        }
        if (regexMode && globMode) {
            return ParsedArgs.error("--regex and --glob are mutually exclusive");
        }
        if (pathMode && allTargetsMode) {
            return ParsedArgs.error("--path and --all-targets are mutually exclusive");
        }
        if (globMode && allTargetsMode) {
            return ParsedArgs.error("--glob and --all-targets are mutually exclusive");
        }

        boolean filesInventoryMode = filesMode
                && (positionals.isEmpty() || (positionals.size() == 1 && filesOptionIndex >= 0 && filesOptionIndex < firstPositionalIndex));
        if (filesInventoryMode) {
            request.remove("query");
            request.put("mode", "listFiles");
            request.put("root", positionals.isEmpty() ? "." : positionals.get(0));
            return ParsedArgs.ok(request, format, textMode);
        }

        if (positionals.size() < 1 || positionals.size() > 2) {
            return ParsedArgs.error("expected QUERY [ROOT]");
        }

        ((ObjectNode) request.get("query")).put("case", request.get("query").has("case") ? request.get("query").get("case").asText() : "sensitive");
        ((ObjectNode) request.get("query")).put("text", positionals.get(0));
        request.put("root", positionals.size() == 2 ? positionals.get(1) : ".");
        if (filesMode) {
            output.put("mode", "summary");
            ensureFileTargets(search);
        }
        return ParsedArgs.ok(request, format, textMode);
    }

    private static ObjectNode encoding(ObjectNode request) {
        JsonNode existing = request.get("encoding");
        if (existing != null && existing.isObject()) {
            return (ObjectNode) existing;
        }
        return request.putObject("encoding");
    }

    private static String valueAfter(String[] args, int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }

    private static Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static void putTargets(ObjectNode search, String... values) {
        ArrayNode targets = search.putArray("targets");
        for (String value : values) {
            targets.add(value);
        }
    }

    private static void ensureFileTargets(ObjectNode search) {
        JsonNode current = search.get("targets");
        List<String> kept = new ArrayList<String>();
        if (current != null && current.isArray()) {
            for (JsonNode node : current) {
                String value = node.asText();
                if (!"directory".equals(value)) {
                    kept.add(value);
                }
            }
        }
        if (kept.isEmpty()) {
            kept.add("content");
        }
        ArrayNode targets = search.putArray("targets");
        for (String value : kept) {
            targets.add(value);
        }
    }

    private static String formatTextResult(MikuGrepResult result, String mode) {
        if ("files".equals(mode)) {
            return formatFilesText(result);
        }
        if ("agent".equals(mode)) {
            return formatAgentText(result);
        }
        JsonNode effectiveRequest = MikuGrepJson.mapper().valueToTree(result.effectiveRequest);
        if (effectiveRequest.has("mode") && "listFiles".equals(effectiveRequest.get("mode").asText())) {
            return formatFilesText(result);
        }
        return formatSummaryText(result);
    }

    private static String formatFilesText(MikuGrepResult result) {
        List<String> files = new ArrayList<String>();
        if (result.files != null && !result.files.isEmpty()) {
            for (FileListEntry file : result.files) {
                files.add(file.path);
            }
        } else {
            files.addAll(uniqueFiles(result));
        }
        return files.isEmpty() ? "" : join(files, "\n") + "\n";
    }

    private static String formatSummaryText(MikuGrepResult result) {
        List<String> lines = new ArrayList<String>();
        lines.add("matches: " + result.summary.matches);
        lines.add("files: " + result.summary.filesMatched);
        if (result.summary.directoriesMatched > 0) {
            lines.add("directories: " + result.summary.directoriesMatched);
        }
        if (result.summary.truncated) {
            lines.add("truncated: " + (result.summary.truncatedReason != null ? result.summary.truncatedReason : "true"));
        }
        if (result.diagnostics != null && !result.diagnostics.isEmpty()) {
            lines.add("diagnostics: " + result.diagnostics.size());
        }
        lines.add("");

        int count = 0;
        for (MikuGrepMatch match : safeMatches(result)) {
            if (count++ >= 20) {
                break;
            }
            appendMatchText(lines, match);
        }
        return join(lines, "\n") + "\n";
    }

    private static void appendMatchText(List<String> lines, MikuGrepMatch match) {
        if (match instanceof FileSummaryMatch) {
            FileSummaryMatch file = (FileSummaryMatch) match;
            lines.add(file.file + "  " + countText(file.matchCount));
            if (file.snippets != null) {
                int snippetCount = 0;
                for (FileSummarySnippet snippet : file.snippets) {
                    if (snippetCount++ >= 3) {
                        break;
                    }
                    lines.add("  " + snippet.line + ": " + snippet.text);
                }
            }
        } else if (match instanceof ContentMatch) {
            ContentMatch content = (ContentMatch) match;
            lines.add(content.file + ":" + content.line + ":" + content.column + ": " + content.text);
            appendContext(lines, content.contextBefore, "-");
            appendContext(lines, content.contextAfter, "+");
        } else if (match instanceof FilepathMatch) {
            lines.add(((FilepathMatch) match).file);
        } else if (match instanceof DirectoryMatch) {
            lines.add(((DirectoryMatch) match).path + "/");
        } else if (match instanceof DirectorySummaryMatch) {
            DirectorySummaryMatch directory = (DirectorySummaryMatch) match;
            lines.add(directory.path + "/  " + countText(directory.matchCount));
        } else if (match instanceof AgentFileMatch) {
            AgentFileMatch file = (AgentFileMatch) match;
            lines.add(file.file + "  " + countText(file.matchCount));
        } else if (match instanceof AgentDirectoryMatch) {
            AgentDirectoryMatch directory = (AgentDirectoryMatch) match;
            lines.add(directory.path + "/  " + countText(directory.matchCount));
        }
    }

    private static void appendContext(List<String> lines, List<ContextLine> contexts, String marker) {
        if (contexts == null) {
            return;
        }
        for (ContextLine context : contexts) {
            lines.add("  " + context.line + marker + " " + context.text);
        }
    }

    private static String formatAgentText(MikuGrepResult result) {
        List<String> lines = new ArrayList<String>();
        lines.add("matches: " + result.summary.matches);
        lines.add("files: " + result.summary.filesMatched);
        if (result.summary.truncated) {
            lines.add("truncated: " + (result.summary.truncatedReason != null ? result.summary.truncatedReason : "true"));
        }
        lines.add("");
        lines.add("top files:");

        List<AgentFileMatch> agentFiles = new ArrayList<AgentFileMatch>();
        for (MikuGrepMatch match : safeMatches(result)) {
            if (match instanceof AgentFileMatch) {
                agentFiles.add((AgentFileMatch) match);
            }
        }
        int fileCount = 0;
        for (AgentFileMatch match : agentFiles) {
            if (fileCount++ >= 10) {
                break;
            }
            lines.add("  " + match.file + "  " + countText(match.matchCount));
            if (match.representativeSnippets != null) {
                int snippetCount = 0;
                for (FileSummarySnippet snippet : match.representativeSnippets) {
                    if (snippetCount++ >= 2) {
                        break;
                    }
                    lines.add("    " + snippet.line + ": " + snippet.text);
                }
            }
        }

        List<String> readRanges = new ArrayList<String>();
        for (AgentFileMatch match : agentFiles) {
            if (match.readRanges == null || match.readRanges.isEmpty()) {
                continue;
            }
            ReadRangeCandidate range = match.readRanges.get(0);
            readRanges.add(match.file + ":" + range.startLine + "-" + range.endLine);
            if (readRanges.size() >= 10) {
                break;
            }
        }
        if (!readRanges.isEmpty()) {
            lines.add("");
            lines.add("next reads:");
            for (String range : readRanges) {
                lines.add("  " + range);
            }
        }
        return join(lines, "\n") + "\n";
    }

    private static String formatTextError(MikuGrepResult result) {
        List<String> lines = new ArrayList<String>();
        lines.add("error: " + (result.error != null ? result.error.code : "unknown")
                + (result.error != null && result.error.message != null ? ": " + result.error.message : ""));
        if (result.diagnostics != null) {
            int count = 0;
            for (Diagnostic diagnostic : result.diagnostics) {
                if (count++ >= 5) {
                    break;
                }
                lines.add(diagnostic.severity.jsonValue() + ": " + diagnostic.code + ": " + diagnostic.message);
            }
        }
        return join(lines, "\n") + "\n";
    }

    private static Set<String> uniqueFiles(MikuGrepResult result) {
        Set<String> files = new LinkedHashSet<String>();
        for (MikuGrepMatch match : safeMatches(result)) {
            if (match instanceof FileSummaryMatch) {
                files.add(((FileSummaryMatch) match).file);
            } else if (match instanceof FilepathMatch) {
                files.add(((FilepathMatch) match).file);
            } else if (match instanceof ContentMatch) {
                files.add(((ContentMatch) match).file);
            } else if (match instanceof AgentFileMatch) {
                files.add(((AgentFileMatch) match).file);
            }
        }
        List<String> sorted = new ArrayList<String>(files);
        Collections.sort(sorted);
        return new LinkedHashSet<String>(sorted);
    }

    private static List<MikuGrepMatch> safeMatches(MikuGrepResult result) {
        return result.matches != null ? result.matches : Collections.<MikuGrepMatch>emptyList();
    }

    private static String countText(Integer count) {
        int value = count != null ? count.intValue() : 0;
        return value + " match" + (value == 1 ? "" : "es");
    }

    private static String join(List<String> values, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private static final class ParsedArgs {
        final boolean ok;
        final String message;
        final ObjectNode request;
        final String format;
        final String textMode;

        private ParsedArgs(boolean ok, String message, ObjectNode request, String format, String textMode) {
            this.ok = ok;
            this.message = message;
            this.request = request;
            this.format = format;
            this.textMode = textMode;
        }

        static ParsedArgs ok(ObjectNode request, String format, String textMode) {
            return new ParsedArgs(true, null, request, format, textMode);
        }

        static ParsedArgs error(String message) {
            return new ParsedArgs(false, message, null, null, null);
        }
    }
}
