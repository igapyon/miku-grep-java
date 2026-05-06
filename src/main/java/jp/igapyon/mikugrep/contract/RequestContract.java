package jp.igapyon.mikugrep.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.igapyon.mikugrep.model.EncodingOptions;
import jp.igapyon.mikugrep.model.IgnoreMode;
import jp.igapyon.mikugrep.model.IgnoreOptions;
import jp.igapyon.mikugrep.model.OutputSort;
import jp.igapyon.mikugrep.model.OutputMode;
import jp.igapyon.mikugrep.model.OutputOptions;
import jp.igapyon.mikugrep.model.SearchOptions;
import jp.igapyon.mikugrep.model.SearchTarget;
import jp.igapyon.mikugrep.model.SupportedEncoding;

public final class RequestContract {
    public static final int VERSION = 1;

    public static final List<String> DEFAULT_EXCLUDE_DIRS = Collections.unmodifiableList(Arrays.asList(
            ".git",
            ".svn",
            "node_modules",
            "target",
            "build",
            "dist",
            ".gradle",
            ".idea",
            ".vscode",
            ".settings",
            "vendor"));

    public static final List<String> DEFAULT_EXCLUDE_FILES = Collections.unmodifiableList(Arrays.asList(
            "*.class",
            "*.jar",
            "*.zip",
            "*.png",
            "*.jpg",
            "*.jpeg",
            "*.gif",
            "*.pdf",
            ".classpath",
            ".project"));

    public static final Limits LIMITS = new Limits();
    public static final Defaults DEFAULTS = new Defaults();
    public static final RequestFieldShape REQUEST_SHAPE = createRequestShape();

    private RequestContract() {
    }

    public static final class Limits {
        public final int regexPatternLength = 1000;
        public final int maxDepth = 50;
        public final long maxFileBytes = 104857600L;
        public final int maxLineChars = 10000000;
        public final int maxFilesVisited = 1000000;
        public final int maxDirectoriesVisited = 100000;
        public final int maxMatches = 10000;
        public final int maxMatchesPerFile = 1000;
        public final int maxLineLength = 4000;
        public final int maxSnippetsPerFile = 100;
        public final int contextLines = 20;

        private Limits() {
        }
    }

    public static final class Defaults {
        private Defaults() {
        }

        public SearchOptions search() {
            SearchOptions search = new SearchOptions();
            search.targets = Collections.singletonList(SearchTarget.CONTENT);
            search.recursive = Boolean.TRUE;
            search.maxDepth = 20;
            search.maxFileBytes = 10485760L;
            search.maxLineChars = 1000000;
            search.maxFilesVisited = 100000;
            search.maxDirectoriesVisited = 10000;
            search.includeFileNamePatterns = Collections.emptyList();
            search.excludeFileNamePatterns = Collections.emptyList();
            search.excludeDirNamePatterns = Collections.emptyList();
            return search;
        }

        public OutputOptions output() {
            OutputOptions output = new OutputOptions();
            output.mode = OutputMode.SUMMARY;
            output.sort = OutputSort.PATH;
            output.maxMatches = 200;
            output.maxMatchesPerFile = 20;
            output.maxLineLength = 240;
            output.maxSnippetsPerFile = 3;
            output.includeReadfileRequestHints = Boolean.FALSE;
            output.contextLinesBefore = 0;
            output.contextLinesAfter = 0;
            return output;
        }

        public EncodingOptions encoding() {
            EncodingOptions encoding = new EncodingOptions();
            encoding.preset = null;
            encoding.defaultEncoding = SupportedEncoding.UTF_8;
            encoding.rules = Collections.emptyList();
            encoding.onDecodeError = "skip";
            return encoding;
        }

        public IgnoreOptions ignore() {
            IgnoreOptions ignore = new IgnoreOptions();
            ignore.mode = IgnoreMode.AUTO;
            ignore.sources = Collections.unmodifiableList(Arrays.asList(".gitignore", ".ignore", ".git/info/exclude"));
            ignore.useGlobalGitignore = Boolean.FALSE;
            ignore.loadedSources = Collections.emptyList();
            return ignore;
        }
    }

    private static RequestFieldShape createRequestShape() {
        return new RequestFieldShape()
                .field("version")
                .field("root")
                .field("detectGitRoot")
                .field("mode")
                .object("query", new RequestFieldShape()
                        .field("type")
                        .field("text")
                        .field("case"))
                .object("search", new RequestFieldShape()
                        .field("targets")
                        .field("recursive")
                        .field("maxDepth")
                        .field("maxFileBytes")
                        .field("maxLineChars")
                        .field("maxFilesVisited")
                        .field("maxDirectoriesVisited")
                        .field("includeFileNamePatterns")
                        .field("excludeFileNamePatterns")
                        .field("excludeDirNamePatterns"))
                .object("output", new RequestFieldShape()
                        .field("mode")
                        .field("sort")
                        .field("maxMatches")
                        .field("maxMatchesPerFile")
                        .field("maxLineLength")
                        .field("maxSnippetsPerFile")
                        .field("includeReadfileRequestHints")
                        .field("contextLines")
                        .field("contextLinesBefore")
                        .field("contextLinesAfter"))
                .object("encoding", new RequestFieldShape()
                        .field("preset")
                        .field("default")
                        .field("rules")
                        .field("onDecodeError"))
                .object("ignore", new RequestFieldShape()
                        .field("mode")
                        .field("sources")
                        .field("useGlobalGitignore"));
    }
}
