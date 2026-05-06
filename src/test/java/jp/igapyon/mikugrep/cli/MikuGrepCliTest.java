package jp.igapyon.mikugrep.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MikuGrepCliTest {
    @TempDir
    Path tempDir;

    @Test
    void printsVersion() throws Exception {
        CliRun run = run(new String[] { "--version" }, "");

        assertEquals(0, run.exitCode);
        assertEquals("miku-grep 0.9.0\n", run.stdout);
        assertEquals("", run.stderr);
    }

    @Test
    void rejectsUnknownOption() throws Exception {
        CliRun run = run(new String[] { "--bad" }, "");

        assertEquals(2, run.exitCode);
        assertEquals("", run.stdout);
        assertEquals("usage: miku-grep [--version|--help]\n", run.stderr);
    }

    @Test
    void helpExplainsAgentRequestContract() throws Exception {
        CliRun run = run(new String[] { "--help" }, "");

        assertEquals(0, run.exitCode);
        assertEquals("", run.stderr);
        assertTrue(run.stdout.contains("USAGE"));
        assertTrue(run.stdout.contains("MINIMAL REQUEST"));
        assertTrue(run.stdout.contains("REQUEST FIELDS"));
        assertTrue(run.stdout.contains("RESULT SHAPE"));
        assertTrue(run.stdout.contains("FULL STDIN / STDOUT EXAMPLE"));
        assertTrue(run.stdout.contains("Possible successful output"));
        assertTrue(run.stdout.contains("Possible validation error output"));
        assertTrue(run.stdout.contains("COMMON DIAGNOSTIC CODES"));
        assertTrue(run.stdout.contains("\"version\": 1"));
        assertTrue(run.stdout.contains("search.targets"));
        assertTrue(run.stdout.contains("output.mode"));
        assertTrue(run.stdout.contains("encoding.rules"));
        assertTrue(run.stdout.contains("detectGitRoot"));
        assertTrue(run.stdout.contains("encoding.preset"));
        assertTrue(run.stdout.contains("japanese-legacy"));
        assertTrue(run.stdout.contains("readfileHints"));
        assertTrue(run.stdout.contains("agentFile"));
        assertTrue(run.stdout.contains("agentDirectory"));
        assertTrue(run.stdout.contains("invalid_mode"));
        assertTrue(run.stdout.contains("invalid_query_case"));
        assertTrue(run.stdout.contains("invalid_output_sort"));
        assertTrue(run.stdout.contains("invalid_encoding_preset"));
        assertTrue(run.stdout.contains("Case-insensitive content search"));
        assertTrue(run.stdout.contains("File inventory"));
        assertTrue(run.stdout.contains("Agent summary with relevance sort"));
    }

    @Test
    void rejectsMalformedStdin() throws Exception {
        CliRun run = run(new String[0], "{");

        assertEquals(2, run.exitCode);
        assertEquals("", run.stdout);
        assertTrue(run.stderr.startsWith("malformed stdin: "));
    }

    @Test
    void writesResultJsonToStdout() throws Exception {
        Files.write(tempDir.resolve("README.md"), "RepositoryMap\n".getBytes(StandardCharsets.UTF_8));

        CliRun run = run(new String[0], request(tempDir.toString(), "RepositoryMap"));

        assertEquals(0, run.exitCode);
        assertEquals("", run.stderr);
        assertTrue(run.stdout.contains("\"ok\" : true"));
        assertTrue(run.stdout.contains("\"file\" : \"README.md\""));
    }

    @Test
    void returnsOneForRequestFailure() throws Exception {
        CliRun run = run(new String[0], "{\"version\":2}");

        assertEquals(1, run.exitCode);
        assertEquals("", run.stderr);
        assertTrue(run.stdout.contains("\"ok\" : false"));
        assertTrue(run.stdout.contains("\"code\" : \"invalid_version\""));
    }

    private static CliRun run(String[] args, String stdin) throws Exception {
        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(stdoutBytes, true, "UTF-8");
        PrintStream stderr = new PrintStream(stderrBytes, true, "UTF-8");
        int exitCode = MikuGrepCli.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), stdout, stderr);
        return new CliRun(exitCode, stdoutBytes.toString("UTF-8"), stderrBytes.toString("UTF-8"));
    }

    private static String request(String root, String query) {
        return "{"
                + "\"version\":1,"
                + "\"root\":\"" + root.replace("\\", "\\\\") + "\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"" + query + "\"}"
                + "}";
    }

    private static final class CliRun {
        final int exitCode;
        final String stdout;
        final String stderr;

        CliRun(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
