package jp.igapyon.mikugrep.result;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.igapyon.mikugrep.contract.RequestContract;
import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.MikuGrepError;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.MikuGrepResult;
import jp.igapyon.mikugrep.model.Summary;

public final class ResultBuilder {
    private ResultBuilder() {
    }

    public static Summary createSummary() {
        Summary summary = new Summary();
        summary.filesVisited = 0;
        summary.filesScanned = 0;
        summary.filesMatched = 0;
        summary.matches = 0;
        summary.diagnostics = 0;
        summary.truncated = false;
        summary.truncatedReason = null;
        return summary;
    }

    public static MikuGrepResult finish(
            boolean ok,
            String code,
            String message,
            Object effectiveRequest,
            List<MikuGrepMatch> matches,
            Summary summary,
            List<Diagnostic> diagnostics) {
        MikuGrepResult result = new MikuGrepResult();
        result.version = RequestContract.VERSION;
        result.ok = ok;
        result.error = ok ? null : errorOrDefault(code, message);
        result.effectiveRequest = effectiveRequest;
        result.matches = matches;
        result.summary = summaryWithDiagnosticCount(summary, diagnostics);
        result.diagnostics = sortDiagnostics(diagnostics);
        return result;
    }

    public static List<Diagnostic> sortDiagnostics(List<Diagnostic> diagnostics) {
        Collections.sort(diagnostics, new Comparator<Diagnostic>() {
            public int compare(Diagnostic left, Diagnostic right) {
                int pathCompare = diagnosticPath(left).compareTo(diagnosticPath(right));
                if (pathCompare != 0) {
                    return pathCompare;
                }
                int lineCompare = Integer.compare(diagnosticLine(left), diagnosticLine(right));
                if (lineCompare != 0) {
                    return lineCompare;
                }
                return diagnosticCode(left).compareTo(diagnosticCode(right));
            }
        });
        return diagnostics;
    }

    private static MikuGrepError errorOrDefault(String code, String message) {
        MikuGrepError error = new MikuGrepError();
        error.code = code == null ? "invalid_request" : code;
        error.message = message == null ? "request failed" : message;
        return error;
    }

    private static Summary summaryWithDiagnosticCount(Summary source, List<Diagnostic> diagnostics) {
        Summary summary = new Summary();
        summary.filesVisited = source.filesVisited;
        summary.filesScanned = source.filesScanned;
        summary.filesMatched = source.filesMatched;
        summary.matches = source.matches;
        summary.diagnostics = diagnostics.size();
        summary.truncated = source.truncated;
        summary.truncatedReason = source.truncatedReason;
        return summary;
    }

    private static String diagnosticPath(Diagnostic diagnostic) {
        if (diagnostic.file != null) {
            return diagnostic.file;
        }
        if (diagnostic.path != null) {
            return diagnostic.path;
        }
        return "";
    }

    private static int diagnosticLine(Diagnostic diagnostic) {
        return diagnostic.line == null ? 0 : diagnostic.line.intValue();
    }

    private static String diagnosticCode(Diagnostic diagnostic) {
        return diagnostic.code == null ? "" : diagnostic.code;
    }
}
