package jp.igapyon.mikugrep.search;

import java.io.IOException;
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

import jp.igapyon.mikugrep.glob.Glob;
import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.DiagnosticSeverity;
import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.FileListEntry;
import jp.igapyon.mikugrep.model.FileListSummary;
import jp.igapyon.mikugrep.model.FileListSummaryCount;
import jp.igapyon.mikugrep.model.IgnoreMode;
import jp.igapyon.mikugrep.model.IgnoreLoadedSource;
import jp.igapyon.mikugrep.model.Query;
import jp.igapyon.mikugrep.model.Summary;
import jp.igapyon.mikugrep.pathsecurity.PathSecurity;
import jp.igapyon.mikugrep.result.ResultBuilder;

public final class ListFiles {
    private ListFiles() {
    }

    public static ListFilesResult runListFiles(EffectiveRequest request, String rootPath, List<Diagnostic> diagnostics) {
        State state = new State(request, rootPath, diagnostics);
        traverse(state, Paths.get(rootPath), "", 0, Collections.<IgnoreRule>emptyList());
        Collections.sort(state.files, new Comparator<FileListEntry>() {
            public int compare(FileListEntry left, FileListEntry right) {
                return left.path.compareTo(right.path);
            }
        });
        state.summary.diagnostics = diagnostics.size();
        return new ListFilesResult(state.files, summarizeFiles(state.files), state.summary);
    }

    private static void traverse(State state, Path absoluteDir, String relativeDir, int depth, List<IgnoreRule> inheritedIgnoreRules) {
        if (state.globalLimitReached) {
            return;
        }
        if (state.directoriesEntered >= state.request.search.maxDirectoriesVisited.intValue()) {
            markTruncated(state, "max_directories_visited", "file listing stopped because maxDirectoriesVisited was reached",
                    details("maxDirectoriesVisited", state.request.search.maxDirectoriesVisited));
            state.globalLimitReached = true;
            return;
        }
        state.directoriesEntered++;
        if (relativeDir.length() == 0) {
            state.summary.directoriesVisited++;
        }

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
                    "directory could not be read and was skipped", relativeDir.length() == 0 ? "." : relativeDir, true, null));
            return;
        }
        Collections.sort(entries, new Comparator<Path>() {
            public int compare(Path left, Path right) {
                return left.getFileName().toString().compareTo(right.getFileName().toString());
            }
        });

        List<IgnoreRule> ignoreRules = new ArrayList<IgnoreRule>(inheritedIgnoreRules);
        ignoreRules.addAll(loadIgnoreRulesForDirectory(state, safeDir, relativeDir));

        for (Path entry : entries) {
            if (state.globalLimitReached) {
                return;
            }
            String name = entry.getFileName().toString();
            String relativePath = relativeDir.length() == 0 ? name : relativeDir + "/" + name;
            if (Files.isSymbolicLink(entry)) {
                state.diagnostics.add(diagnostic(DiagnosticSeverity.INFO, "symlink_skipped", "symlink was skipped", relativePath, true, null));
                continue;
            }
            if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                if (Glob.matchesAny(name, state.request.search.excludeDirNamePatterns)) {
                    continue;
                }
                if (isIgnoredByRules(ignoreRules, relativePath, true)) {
                    state.summary.directoriesIgnored++;
                    continue;
                }
                state.summary.directoriesVisited++;
                if (!state.request.search.recursive.booleanValue() || depth >= state.request.search.maxDepth.intValue()) {
                    continue;
                }
                traverse(state, entry, relativePath, depth + 1, ignoreRules);
                continue;
            }
            if (!Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            if (state.summary.filesVisited >= state.request.search.maxFilesVisited.intValue()) {
                markTruncated(state, "max_files_visited", "file listing stopped because maxFilesVisited was reached",
                        details("maxFilesVisited", state.request.search.maxFilesVisited));
                state.globalLimitReached = true;
                return;
            }
            state.summary.filesVisited++;
            if (isIgnoredByRules(ignoreRules, relativePath, false)) {
                state.summary.filesIgnored++;
                continue;
            }
            if (!candidateFile(state, name)) {
                continue;
            }
            state.summary.filesScanned++;
            if (state.request.query != null && SearchFind.findMatches(relativePath, state.request.query).isEmpty()) {
                continue;
            }
            FileListEntry file = new FileListEntry();
            file.path = relativePath;
            file.extension = extension(name);
            file.directory = relativeDir.length() == 0 ? "." : relativeDir;
            state.files.add(file);
        }
    }

    private static boolean candidateFile(State state, String basename) {
        List<String> include = state.request.search.includeFileNamePatterns;
        List<String> exclude = state.request.search.excludeFileNamePatterns;
        if (!include.isEmpty() && !Glob.matchesAny(basename, include)) {
            return false;
        }
        return !Glob.matchesAny(basename, exclude);
    }

    private static Path resolveInsideRoot(State state, Path absolutePath, String relativePath) {
        try {
            Path realPath = absolutePath.toRealPath();
            if (!PathSecurity.isPathInsideOrSame(realPath.toString(), state.rootPath)) {
                state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "path_escape_skipped", "path resolved outside root and was skipped", relativePath, true, null));
                return null;
            }
            return realPath;
        } catch (IOException ex) {
            state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "file_not_readable", "path could not be resolved and was skipped", relativePath, true, null));
            return null;
        }
    }

    private static FileListSummary summarizeFiles(List<FileListEntry> files) {
        FileListSummary summary = new FileListSummary();
        summary.files = Integer.valueOf(files.size());
        List<String> extensions = new ArrayList<String>();
        List<String> directories = new ArrayList<String>();
        for (FileListEntry file : files) {
            extensions.add(file.extension);
            directories.add(file.directory);
        }
        summary.extensions = summarizeBy(extensions, true);
        summary.directories = summarizeBy(directories, false);
        return summary;
    }

    private static List<FileListSummaryCount> summarizeBy(List<String> values, final boolean extension) {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (String value : values) {
            Integer count = counts.get(value);
            counts.put(value, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
        }
        List<FileListSummaryCount> result = new ArrayList<FileListSummaryCount>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            FileListSummaryCount count = new FileListSummaryCount();
            if (extension) {
                count.extension = entry.getKey();
            } else {
                count.path = entry.getKey();
            }
            count.count = entry.getValue();
            result.add(count);
        }
        Collections.sort(result, new Comparator<FileListSummaryCount>() {
            public int compare(FileListSummaryCount left, FileListSummaryCount right) {
                int count = right.count.intValue() - left.count.intValue();
                if (count != 0) {
                    return count;
                }
                String leftKey = extension ? left.extension : left.path;
                String rightKey = extension ? right.extension : right.path;
                return leftKey.compareTo(rightKey);
            }
        });
        return result;
    }

    private static String extension(String basename) {
        int dot = basename.lastIndexOf('.');
        return dot < 0 ? "" : basename.substring(dot).toLowerCase();
    }

    private static void markTruncated(State state, String reason, String message, Map<String, Object> details) {
        if (!state.summary.truncated) {
            state.summary.truncated = true;
            state.summary.truncatedReason = reason;
        }
        state.diagnostics.add(diagnostic(DiagnosticSeverity.INFO, reason, message, null, null, details));
    }

    private static Diagnostic diagnostic(DiagnosticSeverity severity, String code, String message, String path, Boolean skipped, Map<String, Object> details) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.severity = severity;
        diagnostic.code = code;
        diagnostic.message = message;
        diagnostic.path = path;
        diagnostic.skipped = skipped;
        diagnostic.details = details;
        return diagnostic;
    }

    private static Map<String, Object> details(String key, Object value) {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put(key, value);
        return details;
    }

    private static List<IgnoreRule> loadIgnoreRulesForDirectory(State state, Path absoluteDir, String relativeDir) {
        if (state.request.ignore.mode == IgnoreMode.NONE) {
            return Collections.emptyList();
        }
        List<IgnoreRule> rules = new ArrayList<IgnoreRule>();
        for (String source : sourcesForDirectory(state.request.ignore.sources, relativeDir)) {
            String sourcePath = relativeDir.length() == 0 ? source : relativeDir + "/" + source;
            Path absolutePath = absoluteDir.resolve(source);
            List<String> lines;
            try {
                if (!Files.exists(absolutePath, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }
                lines = Files.readAllLines(absolutePath, java.nio.charset.StandardCharsets.UTF_8);
            } catch (IOException ex) {
                state.diagnostics.add(diagnostic(DiagnosticSeverity.WARNING, "ignore_file_not_readable", "ignore file could not be read and was skipped", sourcePath, true, null));
                continue;
            }
            IgnoreLoadedSource loaded = new IgnoreLoadedSource();
            loaded.path = sourcePath;
            loaded.baseDirectory = relativeDir.length() == 0 ? "." : relativeDir;
            loaded.patterns = Integer.valueOf(0);
            loaded.unsupportedPatterns = Integer.valueOf(0);
            rules.addAll(parseIgnoreFile(lines, sourcePath, loaded.baseDirectory, state, loaded));
            state.request.ignore.loadedSources.add(loaded);
        }
        return rules;
    }

    private static boolean isIgnoredByRules(List<IgnoreRule> rules, String relativePath, boolean directory) {
        boolean ignored = false;
        for (IgnoreRule rule : rules) {
            if (ruleMatches(rule, relativePath, directory)) {
                ignored = !rule.negated;
            }
        }
        return ignored;
    }

    private static List<String> sourcesForDirectory(List<String> sources, String relativeDir) {
        List<String> result = new ArrayList<String>();
        for (String source : sources) {
            if (".gitignore".equals(source) || ".ignore".equals(source) || (relativeDir.length() == 0 && ".git/info/exclude".equals(source))) {
                result.add(source);
            }
        }
        return result;
    }

    private static List<IgnoreRule> parseIgnoreFile(List<String> lines, String sourcePath, String baseDirectory, State state, IgnoreLoadedSource loaded) {
        List<IgnoreRule> rules = new ArrayList<IgnoreRule>();
        for (int index = 0; index < lines.size(); index++) {
            String trimmed = lines.get(index).trim();
            if (trimmed.length() == 0 || trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.startsWith("\\#") || trimmed.startsWith("\\!") || trimmed.matches(".*[\\[\\]{}].*")) {
                loaded.unsupportedPatterns = Integer.valueOf(loaded.unsupportedPatterns.intValue() + 1);
                Diagnostic diagnostic = diagnostic(DiagnosticSeverity.WARNING, "unsupported_ignore_pattern", "ignore pattern is not supported and was skipped", sourcePath, true, details("pattern", trimmed));
                diagnostic.line = Integer.valueOf(index + 1);
                state.diagnostics.add(diagnostic);
                continue;
            }
            boolean negated = trimmed.startsWith("!");
            String patternText = negated ? trimmed.substring(1) : trimmed;
            boolean anchored = patternText.startsWith("/");
            boolean directoryOnly = patternText.endsWith("/");
            String pattern = patternText.replaceAll("^/+", "").replaceAll("/+$", "");
            if (pattern.length() == 0) {
                continue;
            }
            loaded.patterns = Integer.valueOf(loaded.patterns.intValue() + 1);
            rules.add(new IgnoreRule(baseDirectory, pattern, negated, directoryOnly, anchored, pattern.indexOf('/') >= 0));
        }
        return rules;
    }

    private static boolean ruleMatches(IgnoreRule rule, String relativePath, boolean directory) {
        if (rule.directoryOnly && !directory) {
            return false;
        }
        String relativeToBase = relativeFromBase(rule.baseDirectory, relativePath);
        if (relativeToBase == null || relativeToBase.length() == 0) {
            return false;
        }
        if (!rule.hasSlash && !rule.anchored) {
            return Glob.globMatch(Paths.get(relativeToBase).getFileName().toString(), rule.pattern);
        }
        return Glob.pathGlobMatch(relativeToBase, rule.pattern);
    }

    private static String relativeFromBase(String baseDirectory, String relativePath) {
        if (".".equals(baseDirectory)) {
            return relativePath;
        }
        if (relativePath.equals(baseDirectory)) {
            return "";
        }
        String prefix = baseDirectory + "/";
        return relativePath.startsWith(prefix) ? relativePath.substring(prefix.length()) : null;
    }

    private static final class State {
        final EffectiveRequest request;
        final String rootPath;
        final List<Diagnostic> diagnostics;
        final Summary summary;
        final List<FileListEntry> files = new ArrayList<FileListEntry>();
        int directoriesEntered;
        boolean globalLimitReached;

        State(EffectiveRequest request, String rootPath, List<Diagnostic> diagnostics) {
            this.request = request;
            this.rootPath = rootPath;
            this.diagnostics = diagnostics;
            this.summary = ResultBuilder.createSummary();
        }
    }

    static final class IgnoreRule {
        final String baseDirectory;
        final String pattern;
        final boolean negated;
        final boolean directoryOnly;
        final boolean anchored;
        final boolean hasSlash;

        IgnoreRule(String baseDirectory, String pattern, boolean negated, boolean directoryOnly, boolean anchored, boolean hasSlash) {
            this.baseDirectory = baseDirectory;
            this.pattern = pattern;
            this.negated = negated;
            this.directoryOnly = directoryOnly;
            this.anchored = anchored;
            this.hasSlash = hasSlash;
        }
    }

    static final class SearchFind {
        static List<Object> findMatches(String text, Query query) {
            if (query.type == jp.igapyon.mikugrep.model.QueryType.GLOB) {
                return Glob.pathGlobMatch(text, query.text) ? Collections.<Object>singletonList(new Object()) : Collections.<Object>emptyList();
            }
            return Collections.emptyList();
        }
    }
}
