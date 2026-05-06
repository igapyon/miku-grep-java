package jp.igapyon.mikugrep.coreapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikugrep.json.MikuGrepJson;
import jp.igapyon.mikugrep.model.AgentFileMatch;
import jp.igapyon.mikugrep.model.FileListEntry;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.MikuGrepResult;

class MikuGrepTest {
    @TempDir
    Path tempDir;

    @Test
    void runRequestReturnsSearchResult() throws Exception {
        Files.write(tempDir.resolve("README.md"), "RepositoryMap\n".getBytes(StandardCharsets.UTF_8));

        MikuGrepResult result = MikuGrep.runRequest(MikuGrepJson.readTree(request(tempDir.toString(), "RepositoryMap")));

        assertTrue(result.ok);
        assertEquals(1, result.summary.matches);
        assertEquals("README.md", ((FileSummaryMatch) result.matches.get(0)).file);
    }

    @Test
    void runRequestReturnsValidationErrorResult() throws Exception {
        MikuGrepResult result = MikuGrep.runRequest(MikuGrepJson.readTree("{\"version\":2}"));

        assertFalse(result.ok);
        assertEquals("invalid_version", result.error.code);
        assertEquals(1, result.summary.diagnostics);
        assertEquals("invalid_version", result.diagnostics.get(0).code);
    }

    @Test
    void runRequestReturnsRootNotFoundResult() throws Exception {
        Path missing = tempDir.resolve("missing");
        MikuGrepResult result = MikuGrep.runRequest(MikuGrepJson.readTree(request(missing.toString(), "RepositoryMap")));

        assertFalse(result.ok);
        assertEquals("root_not_found", result.error.code);
        assertEquals("root_not_found", result.diagnostics.get(0).code);
    }

    @Test
    void runRequestCanListFilesWithGlobQuery() throws Exception {
        Files.createDirectories(tempDir.resolve("src"));
        Files.write(tempDir.resolve("README.md"), "readme\n".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("src/App.java"), "class App {}\n".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("notes.txt"), "note\n".getBytes(StandardCharsets.UTF_8));

        MikuGrepResult result = MikuGrep.runRequest(MikuGrepJson.readTree("{"
                + "\"version\":1,"
                + "\"root\":\"" + tempDir.toString().replace("\\", "\\\\") + "\","
                + "\"mode\":\"listFiles\","
                + "\"query\":{\"type\":\"glob\",\"text\":\"**/*.java\"}"
                + "}"));

        assertTrue(result.ok);
        assertEquals(0, result.matches.size());
        assertEquals(1, result.files.size());
        FileListEntry file = result.files.get(0);
        assertEquals("src/App.java", file.path);
        assertEquals(".java", file.extension);
        assertEquals("src", file.directory);
        assertEquals(Integer.valueOf(1), result.fileSummary.files);
    }

    @Test
    void runRequestSupportsAgentModeRelevanceAndReadfileHints() throws Exception {
        Files.createDirectories(tempDir.resolve("src"));
        Files.write(tempDir.resolve("README.md"), "RepositoryMap\n".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("src/App.java"), "RepositoryMap\nRepositoryMap\n".getBytes(StandardCharsets.UTF_8));

        MikuGrepResult result = MikuGrep.runRequest(MikuGrepJson.readTree("{"
                + "\"version\":1,"
                + "\"root\":\"" + tempDir.toString().replace("\\", "\\\\") + "\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\",\"case\":\"insensitive\"},"
                + "\"output\":{\"mode\":\"agent\",\"sort\":\"relevance\",\"includeReadfileRequestHints\":true}"
                + "}"));

        assertTrue(result.ok);
        assertTrue(result.matches.get(0) instanceof AgentFileMatch);
        AgentFileMatch first = (AgentFileMatch) result.matches.get(0);
        assertEquals("README.md", first.file);
        assertTrue(first.relevance.reasons.contains("readme"));
        assertEquals(2, result.readfileHints.size());
        assertEquals("README.md", result.readfileHints.get(0).file);
    }

    private static String request(String root, String query) {
        return "{"
                + "\"version\":1,"
                + "\"root\":\"" + root.replace("\\", "\\\\") + "\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"" + query + "\"}"
                + "}";
    }
}
