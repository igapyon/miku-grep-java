package jp.igapyon.mikugrep.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.DiagnosticSeverity;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.MikuGrepResult;
import jp.igapyon.mikugrep.model.Summary;

class ResultBuilderTest {
    @Test
    void createsBaseSummary() {
        Summary summary = ResultBuilder.createSummary();

        assertEquals(0, summary.filesVisited);
        assertEquals(0, summary.filesScanned);
        assertEquals(0, summary.filesMatched);
        assertEquals(0, summary.matches);
        assertEquals(0, summary.diagnostics);
        assertFalse(summary.truncated);
        assertNull(summary.truncatedReason);
    }

    @Test
    void finishesSuccessfulResultWithDiagnosticCountAndSortedDiagnostics() {
        Summary summary = ResultBuilder.createSummary();
        summary.filesVisited = 2;
        summary.filesScanned = 1;
        summary.filesMatched = 1;
        summary.matches = 1;

        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        diagnostics.add(diagnostic("warning", "decode_error", "z-bad.txt", null, null));
        diagnostics.add(diagnostic("info", "symlink_skipped", null, "a-link.txt", null));
        diagnostics.add(diagnostic("warning", "b_code", "same.txt", null, Integer.valueOf(2)));
        diagnostics.add(diagnostic("warning", "a_code", "same.txt", null, Integer.valueOf(2)));
        diagnostics.add(diagnostic("warning", "line_first", "same.txt", null, Integer.valueOf(1)));

        MikuGrepResult result = ResultBuilder.finish(
                true,
                null,
                null,
                new HashMap<String, Object>(),
                Collections.<MikuGrepMatch>emptyList(),
                summary,
                diagnostics);

        assertEquals(1, result.version);
        assertTrue(result.ok);
        assertNull(result.error);
        assertEquals(5, result.summary.diagnostics);
        assertEquals(0, summary.diagnostics);
        assertEquals("a-link.txt", result.diagnostics.get(0).path);
        assertEquals("line_first", result.diagnostics.get(1).code);
        assertEquals("a_code", result.diagnostics.get(2).code);
        assertEquals("b_code", result.diagnostics.get(3).code);
        assertEquals("z-bad.txt", result.diagnostics.get(4).file);
    }

    @Test
    void finishesFailedResultWithDefaultErrorWhenNeeded() {
        MikuGrepResult result = ResultBuilder.finish(
                false,
                null,
                null,
                new HashMap<String, Object>(),
                Collections.<MikuGrepMatch>emptyList(),
                ResultBuilder.createSummary(),
                Collections.<Diagnostic>emptyList());

        assertFalse(result.ok);
        assertEquals("invalid_request", result.error.code);
        assertEquals("request failed", result.error.message);
    }

    @Test
    void finishesFailedResultWithGivenError() {
        MikuGrepResult result = ResultBuilder.finish(
                false,
                "unknown_field",
                "unknown field: typo",
                new HashMap<String, Object>(),
                Collections.<MikuGrepMatch>emptyList(),
                ResultBuilder.createSummary(),
                Collections.<Diagnostic>emptyList());

        assertFalse(result.ok);
        assertEquals("unknown_field", result.error.code);
        assertEquals("unknown field: typo", result.error.message);
    }

    private static Diagnostic diagnostic(String severity, String code, String file, String path, Integer line) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.severity = "error".equals(severity) ? DiagnosticSeverity.ERROR
                : "warning".equals(severity) ? DiagnosticSeverity.WARNING : DiagnosticSeverity.INFO;
        diagnostic.code = code;
        diagnostic.message = code;
        diagnostic.file = file;
        diagnostic.path = path;
        diagnostic.line = line;
        return diagnostic;
    }
}
