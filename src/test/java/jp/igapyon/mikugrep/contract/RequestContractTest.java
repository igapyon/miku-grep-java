package jp.igapyon.mikugrep.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikugrep.json.MikuGrepJson;
import jp.igapyon.mikugrep.model.EncodingOptions;
import jp.igapyon.mikugrep.model.IgnoreMode;
import jp.igapyon.mikugrep.model.IgnoreOptions;
import jp.igapyon.mikugrep.model.OutputMode;
import jp.igapyon.mikugrep.model.OutputOptions;
import jp.igapyon.mikugrep.model.SearchOptions;
import jp.igapyon.mikugrep.model.SearchTarget;
import jp.igapyon.mikugrep.model.SupportedEncoding;

class RequestContractTest {
    @Test
    void keepsUpstreamVersionAndDefaultExcludes() {
        assertEquals(1, RequestContract.VERSION);
        assertEquals(Arrays.asList(
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
                "vendor"), RequestContract.DEFAULT_EXCLUDE_DIRS);
        assertEquals(Arrays.asList(
                "*.class",
                "*.jar",
                "*.zip",
                "*.png",
                "*.jpg",
                "*.jpeg",
                "*.gif",
                "*.pdf",
                ".classpath",
                ".project"), RequestContract.DEFAULT_EXCLUDE_FILES);
    }

    @Test
    void keepsUpstreamLimits() {
        assertEquals(1000, RequestContract.LIMITS.regexPatternLength);
        assertEquals(50, RequestContract.LIMITS.maxDepth);
        assertEquals(104857600L, RequestContract.LIMITS.maxFileBytes);
        assertEquals(10000000, RequestContract.LIMITS.maxLineChars);
        assertEquals(1000000, RequestContract.LIMITS.maxFilesVisited);
        assertEquals(100000, RequestContract.LIMITS.maxDirectoriesVisited);
        assertEquals(10000, RequestContract.LIMITS.maxMatches);
        assertEquals(1000, RequestContract.LIMITS.maxMatchesPerFile);
        assertEquals(4000, RequestContract.LIMITS.maxLineLength);
        assertEquals(100, RequestContract.LIMITS.maxSnippetsPerFile);
        assertEquals(20, RequestContract.LIMITS.contextLines);
    }

    @Test
    void createsUpstreamDefaults() {
        SearchOptions search = RequestContract.DEFAULTS.search();
        assertEquals(Arrays.asList(SearchTarget.CONTENT), search.targets);
        assertEquals(Boolean.TRUE, search.recursive);
        assertEquals(Integer.valueOf(20), search.maxDepth);
        assertEquals(Long.valueOf(10485760L), search.maxFileBytes);
        assertEquals(Integer.valueOf(1000000), search.maxLineChars);
        assertEquals(Integer.valueOf(100000), search.maxFilesVisited);
        assertEquals(Integer.valueOf(10000), search.maxDirectoriesVisited);
        assertEquals(0, search.includeFileNamePatterns.size());
        assertEquals(0, search.excludeFileNamePatterns.size());
        assertEquals(0, search.excludeDirNamePatterns.size());

        OutputOptions output = RequestContract.DEFAULTS.output();
        assertEquals(OutputMode.SUMMARY, output.mode);
        assertEquals(Integer.valueOf(200), output.maxMatches);
        assertEquals(Integer.valueOf(20), output.maxMatchesPerFile);
        assertEquals(Integer.valueOf(240), output.maxLineLength);
        assertEquals(Integer.valueOf(3), output.maxSnippetsPerFile);
        assertEquals(Integer.valueOf(0), output.contextLinesBefore);
        assertEquals(Integer.valueOf(0), output.contextLinesAfter);

        EncodingOptions encoding = RequestContract.DEFAULTS.encoding();
        assertEquals(SupportedEncoding.UTF_8, encoding.defaultEncoding);
        assertEquals(0, encoding.rules.size());
        assertEquals("skip", encoding.onDecodeError);

        IgnoreOptions ignore = RequestContract.DEFAULTS.ignore();
        assertEquals(IgnoreMode.AUTO, ignore.mode);
        assertEquals(Arrays.asList(".gitignore", ".ignore", ".git/info/exclude"), ignore.sources);
        assertEquals(Boolean.FALSE, ignore.useGlobalGitignore);
        assertEquals(0, ignore.loadedSources.size());
    }

    @Test
    void keepsRequestShapeForUnknownFieldValidation() throws Exception {
        assertNull(RequestContract.REQUEST_SHAPE.findUnknownField(MikuGrepJson.readTree("{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"},"
                + "\"search\":{\"targets\":[\"content\"]},"
                + "\"output\":{\"mode\":\"summary\"},"
                + "\"encoding\":{\"rules\":[{\"custom\":true}]},"
                + "\"ignore\":{\"mode\":\"auto\"}"
                + "}")));
        assertEquals("query.extra", RequestContract.REQUEST_SHAPE.findUnknownField(MikuGrepJson.readTree("{"
                + "\"version\":1,"
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\",\"extra\":true}"
                + "}")));
        assertEquals("encoding.extra", RequestContract.REQUEST_SHAPE.findUnknownField(MikuGrepJson.readTree("{"
                + "\"version\":1,"
                + "\"encoding\":{\"extra\":true}"
                + "}")));
    }
}
