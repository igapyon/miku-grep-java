package jp.igapyon.mikugrep.search;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.igapyon.mikugrep.glob.Glob;
import jp.igapyon.mikugrep.model.ContentMatch;
import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.DiagnosticSeverity;
import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.EncodingOptions;
import jp.igapyon.mikugrep.model.EncodingRuleInput;
import jp.igapyon.mikugrep.model.EncodingRuleResult;
import jp.igapyon.mikugrep.model.EncodingRuleType;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.FileSummarySnippet;
import jp.igapyon.mikugrep.model.FilenameMatch;
import jp.igapyon.mikugrep.model.MatchType;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.Query;
import jp.igapyon.mikugrep.model.QueryType;
import jp.igapyon.mikugrep.model.Summary;
import jp.igapyon.mikugrep.model.SupportedEncoding;
import jp.igapyon.mikugrep.pathsecurity.PathSecurity;
import jp.igapyon.mikugrep.result.ResultBuilder;

public final class Search {
    private Search() {
    }

    public static SearchResult runSearch(EffectiveRequest request, String rootPath, List<Diagnostic> diagnostics) {
        SearchState state = new SearchState(request, rootPath, diagnostics, ResultBuilder.createSummary());
        traverse(state, Paths.get(rootPath), "", 0);
        List<MikuGrepMatch> matches = request.output.mode == jp.igapyon.mikugrep.model.OutputMode.DETAIL
                ? buildDetailMatches(state)
                : buildFileSummaryMatches(state);
        state.summary.filesMatched = state.summariesByFile.size();
        state.summary.diagnostics = diagnostics.size();
        return new SearchResult(matches, state.summary);
    }

    private static void traverse(SearchState state, Path absoluteDir, String relativeDir, int depth) {
        if (state.globalLimitReached) {
            return;
        }
        if (state.directoriesVisited >= state.request.search.maxDirectoriesVisited.intValue()) {
            markTruncated(state, "max_directories_visited", "search stopped because maxDirectoriesVisited was reached",
                    details("maxDirectoriesVisited", state.request.search.maxDirectoriesVisited));
            state.globalLimitReached = true;
            return;
        }
        state.directoriesVisited++;

        Path safeDir = resolveInsideRoot(state, absoluteDir, relativeDir.length() == 0 ? "." : relativeDir);
        if (safeDir == null) {
            return;
        }

        List<Path> entries = new ArrayList<Path>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(safeDir)) {
            for (Path entry : stream) {
                entries.add(entry);
            }
        } catch (IOException ex) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "directory_not_readable",
                    "directory could not be read and was skipped", null, relativeDir.length() == 0 ? "." : relativeDir, null, true, null));
            return;
        }
        Collections.sort(entries, new Comparator<Path>() {
            public int compare(Path left, Path right) {
                return left.getFileName().toString().compareTo(right.getFileName().toString());
            }
        });

        for (Path entry : entries) {
            if (state.globalLimitReached) {
                return;
            }
            String name = entry.getFileName().toString();
            String relativePath = relativeDir.length() == 0 ? name : relativeDir + "/" + name;
            if (Files.isSymbolicLink(entry)) {
                state.diagnostics.add(diagnostic(DiagnosticSeverity.INFO, "symlink_skipped", "symlink was skipped", null, relativePath, null, true, null));
                continue;
            }
            if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                if (Glob.matchesAny(name, state.request.search.excludeDirNamePatterns)) {
                    continue;
                }
                if (!state.request.search.recursive.booleanValue() || depth >= state.request.search.maxDepth.intValue()) {
                    continue;
                }
                traverse(state, entry, relativePath, depth + 1);
                continue;
            }
            if (!Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            if (state.summary.filesVisited >= state.request.search.maxFilesVisited.intValue()) {
                markTruncated(state, "max_files_visited", "search stopped because maxFilesVisited was reached",
                        details("maxFilesVisited", state.request.search.maxFilesVisited));
                state.globalLimitReached = true;
                return;
            }
            state.summary.filesVisited++;
            if (!candidateFile(state, name)) {
                continue;
            }
            searchFile(state, entry, relativePath, name);
        }
    }

    private static boolean candidateFile(SearchState state, String basename) {
        List<String> include = state.request.search.includeFileNamePatterns;
        List<String> exclude = state.request.search.excludeFileNamePatterns;
        if (!include.isEmpty() && !Glob.matchesAny(basename, include)) {
            return false;
        }
        return !Glob.matchesAny(basename, exclude);
    }

    private static void searchFile(SearchState state, Path absolutePath, String relativePath, String basename) {
        boolean countedScanned = false;
        if (state.request.search.target == jp.igapyon.mikugrep.model.SearchTarget.FILENAME
                || state.request.search.target == jp.igapyon.mikugrep.model.SearchTarget.BOTH) {
            countedScanned = true;
            state.summary.filesScanned++;
            for (Hit hit : findMatches(relativePath, state.request.query)) {
                FilenameMatch match = new FilenameMatch();
                match.file = relativePath;
                match.matchedText = hit.text;
                addHit(state, relativePath, match);
            }
        }
        if (state.request.search.target != jp.igapyon.mikugrep.model.SearchTarget.CONTENT
                && state.request.search.target != jp.igapyon.mikugrep.model.SearchTarget.BOTH) {
            return;
        }

        Path safePath = resolveInsideRoot(state, absolutePath, relativePath);
        if (safePath == null) {
            return;
        }
        if (Files.isSymbolicLink(safePath)) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.INFO, "symlink_skipped", "symlink was skipped", null, relativePath, null, true, null));
            return;
        }
        long size;
        try {
            size = Files.size(safePath);
        } catch (IOException ex) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "file_not_readable", "file could not be read and was skipped", relativePath, null, null, true, null));
            return;
        }
        if (size > state.request.search.maxFileBytes.longValue()) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "max_file_bytes_exceeded",
                    "file exceeded maxFileBytes and was skipped", relativePath, null, null, true,
                    details("size", Long.valueOf(size), "maxFileBytes", state.request.search.maxFileBytes)));
            return;
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(safePath);
        } catch (IOException ex) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "file_not_readable", "file could not be read and was skipped", relativePath, null, null, true, null));
            return;
        }
        if (containsNul(bytes)) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "binary_file_skipped", "binary file was skipped", relativePath, null, null, true, null));
            return;
        }

        EncodingSelection encoding = selectEncoding(state.request.encoding, relativePath, basename);
        String text;
        try {
            text = decode(bytes, encoding.encoding);
        } catch (CharacterCodingException ex) {
            Diagnostic diagnostic = diagnostic(DiagnosticSeverity.WARNING, "decode_error", "file could not be decoded and was skipped",
                    relativePath, null, null, true, null);
            diagnostic.encoding = encoding.encoding;
            diagnostic.encodingRule = encoding.encodingRule;
            state.diagnostics.add(diagnostic);
            return;
        }
        if (!countedScanned) {
            state.summary.filesScanned++;
        }
        if (encoding.encoding == SupportedEncoding.UTF_8 && text.length() > 0 && text.charAt(0) == '\ufeff') {
            text = text.substring(1);
        }

        String[] lines = splitLines(text);
        int fileHitCount = state.summariesByFile.containsKey(relativePath) ? state.summariesByFile.get(relativePath).matchCount.intValue() : 0;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (line.length() > state.request.search.maxLineChars.intValue()) {
                markLineSkipped(state, relativePath, index + 1, line.length());
                continue;
            }
            for (Hit hit : findMatches(line, state.request.query)) {
                if (fileHitCount >= state.request.output.maxMatchesPerFile.intValue()) {
                    markTruncated(state, "max_matches_per_file", "file search stopped because maxMatchesPerFile was reached",
                            details("file", relativePath, "maxMatchesPerFile", state.request.output.maxMatchesPerFile));
                    return;
                }
                Snippet snippet = makeSnippet(line, hit.index, hit.text.length(), state.request.output.maxLineLength.intValue());
                ContentMatch match = new ContentMatch();
                match.file = relativePath;
                match.line = Integer.valueOf(index + 1);
                match.column = Integer.valueOf(hit.index + 1);
                match.matchedText = hit.text;
                match.text = snippet.text;
                match.trimmed = Boolean.valueOf(snippet.trimmed);
                match.textStartColumn = snippet.textStartColumn;
                match.encoding = encoding.encoding;
                match.encodingRule = encoding.encodingRule;
                addHit(state, relativePath, match);
                fileHitCount++;
                if (state.globalLimitReached) {
                    return;
                }
            }
        }
    }

    private static void markLineSkipped(SearchState state, String file, int line, int lineChars) {
        if (!state.summary.truncated) {
            state.summary.truncated = true;
            state.summary.truncatedReason = "max_line_chars_exceeded";
        }
        state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "max_line_chars_exceeded",
                "line exceeded maxLineChars and was skipped", file, null, Integer.valueOf(line), true,
                details("lineChars", Integer.valueOf(lineChars), "maxLineChars", state.request.search.maxLineChars)));
    }

    private static Path resolveInsideRoot(SearchState state, Path absolutePath, String relativePath) {
        Path realPath;
        try {
            realPath = absolutePath.toRealPath();
        } catch (IOException ex) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "file_not_readable", "path could not be resolved and was skipped", null, relativePath, null, true, null));
            return null;
        }
        if (!PathSecurity.isPathInsideOrSame(realPath.toString(), state.rootPath)) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "path_escape_skipped", "path resolved outside root and was skipped", null, relativePath, null, true, null));
            return null;
        }
        return realPath;
    }

    private static void addHit(SearchState state, String file, MikuGrepMatch hit) {
        if (state.summary.matches >= state.request.output.maxMatches.intValue()) {
            markTruncated(state, "max_matches", "search stopped because maxMatches was reached", details("maxMatches", state.request.output.maxMatches));
            state.globalLimitReached = true;
            return;
        }
        state.summary.matches++;
        List<MikuGrepMatch> detail = state.detailsByFile.get(file);
        if (detail == null) {
            detail = new ArrayList<MikuGrepMatch>();
            state.detailsByFile.put(file, detail);
        }
        detail.add(hit);

        FileSummaryMatch summary = state.summariesByFile.get(file);
        if (summary == null) {
            summary = new FileSummaryMatch();
            summary.file = file;
            summary.matchTypes = new ArrayList<MatchType>();
            summary.filenameMatched = Boolean.FALSE;
            summary.contentMatched = Boolean.FALSE;
            summary.lines = new ArrayList<Integer>();
            summary.matchCount = Integer.valueOf(0);
            summary.snippets = new ArrayList<FileSummarySnippet>();
        }
        summary.matchCount = Integer.valueOf(summary.matchCount.intValue() + 1);
        if (hit instanceof FilenameMatch) {
            summary.filenameMatched = Boolean.TRUE;
            if (!summary.matchTypes.contains(MatchType.FILENAME)) {
                summary.matchTypes.add(MatchType.FILENAME);
            }
        } else if (hit instanceof ContentMatch) {
            ContentMatch content = (ContentMatch) hit;
            summary.contentMatched = Boolean.TRUE;
            if (!summary.matchTypes.contains(MatchType.CONTENT)) {
                summary.matchTypes.add(MatchType.CONTENT);
            }
            if (!summary.lines.contains(content.line)) {
                summary.lines.add(content.line);
            }
            if (summary.snippets.size() < state.request.output.maxSnippetsPerFile.intValue()) {
                FileSummarySnippet snippet = new FileSummarySnippet();
                snippet.line = content.line;
                snippet.text = content.text;
                snippet.trimmed = content.trimmed;
                snippet.textStartColumn = content.textStartColumn;
                summary.snippets.add(snippet);
            } else {
                markTruncated(state, "max_snippets_per_file", "snippets were omitted because maxSnippetsPerFile was reached",
                        details("file", file, "maxSnippetsPerFile", state.request.output.maxSnippetsPerFile));
            }
            summary.encoding = content.encoding;
            summary.encodingRule = content.encodingRule;
        }
        state.summariesByFile.put(file, summary);
    }

    private static void markTruncated(SearchState state, String reason, String message, Map<String, Object> details) {
        if (!state.summary.truncated) {
            state.summary.truncated = true;
            state.summary.truncatedReason = reason;
        }
        String key = reason + ":" + details.toString();
        if (state.truncationDiagnosticKeys.contains(key)) {
            return;
        }
        state.truncationDiagnosticKeys.add(key);
        state.diagnostics.add(diagnostic(DiagnosticSeverity.INFO, reason, message, null, null, null, null, details));
    }

    private static List<MikuGrepMatch> buildDetailMatches(SearchState state) {
        List<String> files = new ArrayList<String>(state.detailsByFile.keySet());
        Collections.sort(files);
        List<MikuGrepMatch> result = new ArrayList<MikuGrepMatch>();
        for (String file : files) {
            List<MikuGrepMatch> hits = state.detailsByFile.get(file);
            Collections.sort(hits, new Comparator<MikuGrepMatch>() {
                public int compare(MikuGrepMatch left, MikuGrepMatch right) {
                    int type = typeRank(left) - typeRank(right);
                    if (type != 0) {
                        return type;
                    }
                    int line = contentLine(left) - contentLine(right);
                    if (line != 0) {
                        return line;
                    }
                    return contentColumn(left) - contentColumn(right);
                }
            });
            result.addAll(hits);
        }
        return result;
    }

    private static List<MikuGrepMatch> buildFileSummaryMatches(SearchState state) {
        List<FileSummaryMatch> summaries = new ArrayList<FileSummaryMatch>(state.summariesByFile.values());
        Collections.sort(summaries, new Comparator<FileSummaryMatch>() {
            public int compare(FileSummaryMatch left, FileSummaryMatch right) {
                return left.file.compareTo(right.file);
            }
        });
        List<MikuGrepMatch> result = new ArrayList<MikuGrepMatch>();
        for (FileSummaryMatch summary : summaries) {
            Collections.sort(summary.lines);
            result.add(summary);
        }
        return result;
    }

    private static int typeRank(MikuGrepMatch match) {
        return match instanceof FilenameMatch ? 0 : 1;
    }

    private static int contentLine(MikuGrepMatch match) {
        return match instanceof ContentMatch ? ((ContentMatch) match).line.intValue() : 0;
    }

    private static int contentColumn(MikuGrepMatch match) {
        return match instanceof ContentMatch ? ((ContentMatch) match).column.intValue() : 0;
    }

    private static List<Hit> findMatches(String text, Query query) {
        if (query.type == QueryType.LITERAL) {
            List<Hit> hits = new ArrayList<Hit>();
            int from = 0;
            while (from <= text.length()) {
                int index = text.indexOf(query.text, from);
                if (index < 0) {
                    break;
                }
                hits.add(new Hit(index, query.text));
                from = index + Math.max(query.text.length(), 1);
            }
            return hits;
        }
        Pattern pattern = Pattern.compile(query.text);
        Matcher matcher = pattern.matcher(text);
        List<Hit> hits = new ArrayList<Hit>();
        while (matcher.find()) {
            hits.add(new Hit(matcher.start(), matcher.group()));
        }
        return hits;
    }

    private static Snippet makeSnippet(String line, int matchIndex, int matchLength, int maxLineLength) {
        if (line.length() <= maxLineLength) {
            return new Snippet(line, false, null);
        }
        int matchEnd = matchIndex + matchLength;
        int start = Math.max(0, (int) Math.floor((matchIndex + matchEnd - maxLineLength) / 2.0d));
        if (start + maxLineLength > line.length()) {
            start = Math.max(0, line.length() - maxLineLength);
        }
        Integer textStartColumn = start > 0 ? Integer.valueOf(start + 1) : null;
        return new Snippet(line.substring(start, start + maxLineLength), true, textStartColumn);
    }

    private static String[] splitLines(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
    }

    private static EncodingSelection selectEncoding(EncodingOptions config, String relativePath, String basename) {
        for (EncodingRuleInput rule : config.rules) {
            if (rule.pathPattern != null && Glob.pathGlobMatch(relativePath, rule.pathPattern)) {
                return new EncodingSelection(rule.encoding, encodingRule(EncodingRuleType.PATH_PATTERN, rule.pathPattern));
            }
        }
        for (EncodingRuleInput rule : config.rules) {
            if (rule.fileNamePattern != null && Glob.globMatch(basename, rule.fileNamePattern)) {
                return new EncodingSelection(rule.encoding, encodingRule(EncodingRuleType.FILE_NAME_PATTERN, rule.fileNamePattern));
            }
        }
        return new EncodingSelection(config.defaultEncoding, encodingRule(EncodingRuleType.DEFAULT, null));
    }

    private static EncodingRuleResult encodingRule(EncodingRuleType type, String pattern) {
        EncodingRuleResult result = new EncodingRuleResult();
        result.type = type;
        result.pattern = pattern;
        return result;
    }

    private static String decode(byte[] bytes, SupportedEncoding encoding) throws CharacterCodingException {
        Charset charset = encoding == SupportedEncoding.UTF_8 ? StandardCharsets.UTF_8 : Charset.forName("Shift_JIS");
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }

    private static boolean containsNul(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }

    private static Diagnostic diagnostic(DiagnosticSeverity severity, String code, String message, String file, String path, Integer line, Boolean skipped, Map<String, Object> details) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.severity = severity;
        diagnostic.code = code;
        diagnostic.message = message;
        diagnostic.file = file;
        diagnostic.path = path;
        diagnostic.line = line;
        diagnostic.skipped = skipped;
        diagnostic.details = details;
        return diagnostic;
    }

    private static Map<String, Object> details(String key, Object value) {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put(key, value);
        return details;
    }

    private static Map<String, Object> details(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put(key1, value1);
        details.put(key2, value2);
        return details;
    }

    private static final class Hit {
        final int index;
        final String text;

        Hit(int index, String text) {
            this.index = index;
            this.text = text;
        }
    }

    private static final class Snippet {
        final String text;
        final boolean trimmed;
        final Integer textStartColumn;

        Snippet(String text, boolean trimmed, Integer textStartColumn) {
            this.text = text;
            this.trimmed = trimmed;
            this.textStartColumn = textStartColumn;
        }
    }

    private static final class EncodingSelection {
        final SupportedEncoding encoding;
        final EncodingRuleResult encodingRule;

        EncodingSelection(SupportedEncoding encoding, EncodingRuleResult encodingRule) {
            this.encoding = encoding;
            this.encodingRule = encodingRule;
        }
    }
}
