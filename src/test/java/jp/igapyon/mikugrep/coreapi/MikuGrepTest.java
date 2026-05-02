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

    private static String request(String root, String query) {
        return "{"
                + "\"version\":1,"
                + "\"root\":\"" + root.replace("\\", "\\\\") + "\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"" + query + "\"}"
                + "}";
    }
}
