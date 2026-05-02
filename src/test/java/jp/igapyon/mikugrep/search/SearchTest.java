package jp.igapyon.mikugrep.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikugrep.model.ContentMatch;
import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.FilenameMatch;
import jp.igapyon.mikugrep.model.MatchType;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.validation.Validation;

class SearchTest {
    @TempDir
    Path tempDir;

    @Test
    void returnsFileSummaryMatchesWithDefaultsAndExcludesNodeModules() throws Exception {
        write("README.md", "RepositoryMap\n");
        write("src/RepositoryMap.java", "class RepositoryMap {\n  RepositoryMap field;\n}\n");
        write("node_modules/skip.txt", "RepositoryMap\n");

        SearchResult result = run("{\"target\":\"content\",\"recursive\":true,\"maxDepth\":5}", null, "RepositoryMap");

        assertEquals(2, result.summary.filesVisited);
        assertEquals(2, result.summary.filesMatched);
        assertEquals(3, result.summary.matches);
        assertEquals(2, result.matches.size());
        assertEquals("README.md", ((FileSummaryMatch) result.matches.get(0)).file);
        assertEquals("src/RepositoryMap.java", ((FileSummaryMatch) result.matches.get(1)).file);
    }

    @Test
    void regexContentSearchReturnsMultipleDetailHitsAndZeroLengthHits() throws Exception {
        write("regex.txt", "RepositoryMap RepositoryMap\nRepository\nMap\n");
        SearchResult regex = run(null, "{\"mode\":\"detail\"}", "RepositoryMap", "regex");

        assertEquals(2, regex.matches.size());
        assertContent(regex.matches.get(0), "regex.txt", 1, 1, "RepositoryMap");
        assertContent(regex.matches.get(1), "regex.txt", 1, 15, "RepositoryMap");

        write("zero.txt", "ab\n");
        SearchResult zero = run("{\"includeFileNamePatterns\":[\"zero.txt\"]}", "{\"mode\":\"detail\",\"maxMatchesPerFile\":10}", "(?=a)|(?=b)", "regex");
        assertContent(zero.matches.get(0), "zero.txt", 1, 1, "");
        assertContent(zero.matches.get(1), "zero.txt", 1, 2, "");
    }

    @Test
    void appliesIncludeExcludeDirectoriesDepthAndNonRecursiveTraversal() throws Exception {
        write("root.txt", "RepositoryMap\n");
        write("keep/keep.txt", "RepositoryMap\n");
        write("skip/skip.txt", "RepositoryMap\n");
        write("a/b/two.txt", "RepositoryMap\n");

        SearchResult excluded = run("{\"target\":\"content\",\"excludeDirNamePatterns\":[\"skip\"]}", null, "RepositoryMap");
        assertEquals("a/b/two.txt", ((FileSummaryMatch) excluded.matches.get(0)).file);
        assertEquals("keep/keep.txt", ((FileSummaryMatch) excluded.matches.get(1)).file);
        assertEquals("root.txt", ((FileSummaryMatch) excluded.matches.get(2)).file);

        SearchResult nonRecursive = run("{\"target\":\"content\",\"recursive\":false}", null, "RepositoryMap");
        assertEquals(1, nonRecursive.matches.size());
        assertEquals("root.txt", ((FileSummaryMatch) nonRecursive.matches.get(0)).file);

        SearchResult depthOne = run("{\"target\":\"content\",\"recursive\":true,\"maxDepth\":1}", null, "RepositoryMap");
        assertEquals(3, depthOne.matches.size());
        assertEquals("keep/keep.txt", ((FileSummaryMatch) depthOne.matches.get(0)).file);
        assertEquals("root.txt", ((FileSummaryMatch) depthOne.matches.get(1)).file);
        assertEquals("skip/skip.txt", ((FileSummaryMatch) depthOne.matches.get(2)).file);
    }

    @Test
    void canIncludeDefaultExcludedDirectoriesWhenExcludeDirectoryPatternsAreEmpty() throws Exception {
        write("node_modules/pkg/index.txt", "RepositoryMap\n");

        SearchResult result = run("{\"target\":\"content\",\"excludeDirNamePatterns\":[]}", "{\"mode\":\"detail\"}", "RepositoryMap");

        assertEquals(1, result.matches.size());
        assertContent(result.matches.get(0), "node_modules/pkg/index.txt", 1, 1, "RepositoryMap");
    }

    @Test
    void omittedExcludeDirectoryPatternsKeepDefaultNodeModulesExclusion() throws Exception {
        write("node_modules/pkg/index.txt", "RepositoryMap\n");

        SearchResult result = run("{\"target\":\"content\"}", "{\"mode\":\"detail\"}", "RepositoryMap");

        assertEquals(0, result.matches.size());
    }

    @Test
    void filenameAndBothSearchFollowUpstreamOrderingAndAggregation() throws Exception {
        write("b-RepositoryMap.txt", "RepositoryMap\n");
        write("a-RepositoryMap.txt", "x\nRepositoryMap\n");

        SearchResult detail = run("{\"target\":\"both\"}", "{\"mode\":\"detail\"}", "RepositoryMap");
        assertTrue(detail.matches.get(0) instanceof FilenameMatch);
        assertEquals("a-RepositoryMap.txt", ((FilenameMatch) detail.matches.get(0)).file);
        assertContent(detail.matches.get(1), "a-RepositoryMap.txt", 2, 1, "RepositoryMap");
        assertTrue(detail.matches.get(2) instanceof FilenameMatch);
        assertEquals("b-RepositoryMap.txt", ((FilenameMatch) detail.matches.get(2)).file);
        assertContent(detail.matches.get(3), "b-RepositoryMap.txt", 1, 1, "RepositoryMap");

        SearchResult summary = run("{\"target\":\"both\"}", "{\"mode\":\"file-summary\",\"maxSnippetsPerFile\":1}", "RepositoryMap");
        FileSummaryMatch first = (FileSummaryMatch) summary.matches.get(0);
        assertEquals("a-RepositoryMap.txt", first.file);
        assertEquals(MatchType.FILENAME, first.matchTypes.get(0));
        assertEquals(MatchType.CONTENT, first.matchTypes.get(1));
        assertTrue(first.filenameMatched.booleanValue());
        assertTrue(first.contentMatched.booleanValue());
    }

    @Test
    void filenameSearchMatchesRelativePathsButIncludePatternsUseBasename() throws Exception {
        write("src/App.java", "no content hit\n");
        write("App.java", "no content hit\n");

        SearchResult pathMatch = run("{\"target\":\"filename\",\"includeFileNamePatterns\":[\"App.java\"]}", "{\"mode\":\"detail\"}", "src/App.java");
        assertEquals(1, pathMatch.matches.size());
        assertEquals("src/App.java", ((FilenameMatch) pathMatch.matches.get(0)).file);

        SearchResult includeDoesNotMatchPath = run("{\"target\":\"filename\",\"includeFileNamePatterns\":[\"src/App.java\"]}", "{\"mode\":\"detail\"}", "App.java");
        assertEquals(0, includeDoesNotMatchPath.matches.size());
        assertEquals(0, includeDoesNotMatchPath.summary.filesScanned);
    }

    @Test
    void canIncludeDefaultExcludedZipFilesWhenExcludeFilePatternsAreEmpty() throws Exception {
        write("artifact.zip", "not read for filename search\n");

        SearchResult result = run("{\"target\":\"filename\",\"includeFileNamePatterns\":[\"*.zip\"],\"excludeFileNamePatterns\":[]}", "{\"mode\":\"detail\"}", "\\.zip$", "regex");

        assertEquals(1, result.matches.size());
        assertEquals("artifact.zip", ((FilenameMatch) result.matches.get(0)).file);
    }

    @Test
    void omittedExcludeFilePatternsKeepDefaultZipExclusion() throws Exception {
        write("artifact.zip", "not read for filename search\n");

        SearchResult result = run("{\"target\":\"filename\",\"includeFileNamePatterns\":[\"*.zip\"]}", "{\"mode\":\"detail\"}", "\\.zip$", "regex");

        assertEquals(0, result.matches.size());
    }

    @Test
    void trimsSnippetsAndNormalizesLineEndings() throws Exception {
        write("long.txt", repeat("a", 20) + "RepositoryMap" + repeat("z", 20) + "\n");
        SearchResult longResult = run(null, "{\"mode\":\"detail\",\"maxLineLength\":20}", "RepositoryMap");
        ContentMatch longMatch = (ContentMatch) longResult.matches.get(0);
        assertTrue(longMatch.trimmed.booleanValue());
        assertEquals(Integer.valueOf(17), longMatch.textStartColumn);
        assertEquals("aaaaRepositoryMapzzz", longMatch.text);

        write("lines.txt", "one\nRepositoryMap\r\nthree\rRepositoryMap");
        SearchResult lines = run(null, "{\"mode\":\"detail\"}", "RepositoryMap");
        List<ContentMatch> lineMatches = contentMatches(lines.matches, "lines.txt");
        assertEquals(Integer.valueOf(2), lineMatches.get(0).line);
        assertEquals(Integer.valueOf(4), lineMatches.get(1).line);
    }

    @Test
    void reportsBinarySizeAndDecodeDiagnostics() throws Exception {
        write("binary.txt", new byte[] { 'R', 0, 'M' });
        SearchRun binary = runWithDiagnostics(null, null, "RepositoryMap");
        assertEquals("binary_file_skipped", binary.diagnostics.get(0).code);

        write("big.txt", "RepositoryMap\n");
        SearchRun sizeLimited = runWithDiagnostics("{\"maxFileBytes\":1}", null, "RepositoryMap");
        assertEquals("max_file_bytes_exceeded", findDiagnostic(sizeLimited.diagnostics, "big.txt").code);

        write("bad.txt", new byte[] { (byte) 0xff, (byte) 0xfe, (byte) 0xfd });
        SearchRun bad = runWithDiagnostics("{\"target\":\"both\"}", "{\"mode\":\"detail\"}", "bad");
        assertTrue(bad.result.matches.get(0) instanceof FilenameMatch);
        assertEquals("decode_error", findDiagnostic(bad.diagnostics, "bad.txt").code);
    }

    private SearchResult run(String searchJson, String outputJson, String queryText) throws Exception {
        return run(searchJson, outputJson, queryText, "literal");
    }

    private SearchResult run(String searchJson, String outputJson, String queryText, String queryType) throws Exception {
        return runWithDiagnostics(searchJson, outputJson, queryText, queryType).result;
    }

    private SearchRun runWithDiagnostics(String searchJson, String outputJson, String queryText) throws Exception {
        return runWithDiagnostics(searchJson, outputJson, queryText, "literal");
    }

    private SearchRun runWithDiagnostics(String searchJson, String outputJson, String queryText, String queryType) throws Exception {
        String json = "{"
                + "\"version\":1,"
                + "\"root\":\"" + tempDir.toString().replace("\\", "\\\\") + "\","
                + "\"query\":{\"type\":\"" + queryType + "\",\"text\":\"" + queryText.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
                + (searchJson == null ? "" : ",\"search\":" + searchJson)
                + (outputJson == null ? "" : ",\"output\":" + outputJson)
                + "}";
        EffectiveRequest request = Validation.validateAndNormalize(json).effectiveRequest;
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        return new SearchRun(Search.runSearch(request, tempDir.toRealPath().toString(), diagnostics), diagnostics);
    }

    private void write(String relativePath, String content) throws Exception {
        write(relativePath, content.getBytes(StandardCharsets.UTF_8));
    }

    private void write(String relativePath, byte[] bytes) throws Exception {
        Path path = tempDir.resolve(relativePath);
        Files.createDirectories(path.getParent() == null ? tempDir : path.getParent());
        Files.write(path, bytes);
    }

    private static void assertContent(MikuGrepMatch match, String file, int line, int column, String matchedText) {
        ContentMatch content = (ContentMatch) match;
        assertEquals(file, content.file);
        assertEquals(Integer.valueOf(line), content.line);
        assertEquals(Integer.valueOf(column), content.column);
        assertEquals(matchedText, content.matchedText);
    }

    private static List<ContentMatch> contentMatches(List<MikuGrepMatch> matches, String file) {
        List<ContentMatch> result = new ArrayList<ContentMatch>();
        for (MikuGrepMatch match : matches) {
            if (match instanceof ContentMatch && file.equals(((ContentMatch) match).file)) {
                result.add((ContentMatch) match);
            }
        }
        return result;
    }

    private static Diagnostic findDiagnostic(List<Diagnostic> diagnostics, String file) {
        for (Diagnostic diagnostic : diagnostics) {
            if (file.equals(diagnostic.file)) {
                return diagnostic;
            }
        }
        throw new AssertionError("diagnostic not found: " + file);
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static final class SearchRun {
        final SearchResult result;
        final List<Diagnostic> diagnostics;

        SearchRun(SearchResult result, List<Diagnostic> diagnostics) {
            this.result = result;
            this.diagnostics = diagnostics;
        }
    }
}
