package jp.igapyon.mikugrep.coreapi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.DiagnosticSeverity;
import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.MikuGrepResult;
import jp.igapyon.mikugrep.result.ResultBuilder;
import jp.igapyon.mikugrep.search.Search;
import jp.igapyon.mikugrep.search.SearchResult;
import jp.igapyon.mikugrep.validation.Validation;
import jp.igapyon.mikugrep.validation.ValidationResult;

public final class MikuGrep {
    private MikuGrep() {
    }

    public static MikuGrepResult runRequest(JsonNode request) {
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        ValidationResult validation = Validation.validateAndNormalize(request);

        if (!validation.ok) {
            diagnostics.add(errorDiagnostic(validation.code, validation.message, validation.path));
            return ResultBuilder.finish(false, validation.code, validation.message, Collections.emptyMap(),
                    Collections.<MikuGrepMatch>emptyList(), ResultBuilder.createSummary(), diagnostics);
        }

        EffectiveRequest effectiveRequest = validation.effectiveRequest;
        Path rootPath = Paths.get(effectiveRequest.root).toAbsolutePath().normalize();
        RootCheck rootCheck = checkRoot(rootPath, effectiveRequest.root);
        if (!rootCheck.ok) {
            diagnostics.add(rootCheck.diagnostic);
            return ResultBuilder.finish(false, rootCheck.diagnostic.code, rootCheck.diagnostic.message,
                    effectiveRequest, Collections.<MikuGrepMatch>emptyList(), ResultBuilder.createSummary(), diagnostics);
        }

        SearchResult searchResult = Search.runSearch(effectiveRequest, rootCheck.realPath.toString(), diagnostics);
        return ResultBuilder.finish(true, null, null, effectiveRequest, searchResult.matches, searchResult.summary, diagnostics);
    }

    private static RootCheck checkRoot(Path rootPath, String requestRoot) {
        Path home = homeDirectory();
        if (rootPath.getParent() == null || (home != null && rootPath.equals(home))) {
            return RootCheck.invalid(rootError("root_too_broad", "root is too broad", requestRoot));
        }
        try {
            if (!Files.exists(rootPath)) {
                return RootCheck.invalid(rootError("root_not_found", "root does not exist", requestRoot));
            }
            if (!Files.isDirectory(rootPath)) {
                return RootCheck.invalid(rootError("root_not_accessible", "root is not a directory", requestRoot));
            }
            if (!Files.isReadable(rootPath)) {
                return RootCheck.invalid(rootError("root_not_accessible", "root is not accessible", requestRoot));
            }
            return RootCheck.ok(rootPath.toRealPath());
        } catch (Exception ex) {
            return RootCheck.invalid(rootError("root_not_accessible", "root is not accessible", requestRoot));
        }
    }

    private static Diagnostic rootError(String code, String message, String path) {
        return errorDiagnostic(code, message, path);
    }

    private static Diagnostic errorDiagnostic(String code, String message, String path) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.severity = DiagnosticSeverity.ERROR;
        diagnostic.code = code;
        diagnostic.message = message;
        diagnostic.path = path;
        return diagnostic;
    }

    private static Path homeDirectory() {
        String home = System.getProperty("user.home");
        return home == null || home.length() == 0 ? null : Paths.get(home).toAbsolutePath().normalize();
    }

    private static final class RootCheck {
        final boolean ok;
        final Path realPath;
        final Diagnostic diagnostic;

        private RootCheck(boolean ok, Path realPath, Diagnostic diagnostic) {
            this.ok = ok;
            this.realPath = realPath;
            this.diagnostic = diagnostic;
        }

        static RootCheck ok(Path realPath) {
            return new RootCheck(true, realPath, null);
        }

        static RootCheck invalid(Diagnostic diagnostic) {
            return new RootCheck(false, null, diagnostic);
        }
    }
}
