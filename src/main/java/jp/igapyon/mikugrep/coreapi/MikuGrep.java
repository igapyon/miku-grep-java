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
import jp.igapyon.mikugrep.model.AgentFileMatch;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.FilepathMatch;
import jp.igapyon.mikugrep.model.ContentMatch;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.MikuGrepResult;
import jp.igapyon.mikugrep.model.ReadfileRequest;
import jp.igapyon.mikugrep.model.ReadfileRequestFile;
import jp.igapyon.mikugrep.model.ReadfileRequestHint;
import jp.igapyon.mikugrep.model.RequestMode;
import jp.igapyon.mikugrep.result.ResultBuilder;
import jp.igapyon.mikugrep.search.ListFiles;
import jp.igapyon.mikugrep.search.ListFilesResult;
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
        Path requestedRootPath = Paths.get(effectiveRequest.root).toAbsolutePath().normalize();
        Path rootPath = effectiveRequest.detectGitRoot.booleanValue() ? detectGitRootPath(requestedRootPath) : requestedRootPath;
        if (effectiveRequest.detectGitRoot.booleanValue()) {
            effectiveRequest.root = displayRoot(rootPath);
        }
        RootCheck rootCheck = checkRoot(rootPath, effectiveRequest.root);
        if (!rootCheck.ok) {
            diagnostics.add(rootCheck.diagnostic);
            return ResultBuilder.finish(false, rootCheck.diagnostic.code, rootCheck.diagnostic.message,
                    effectiveRequest, Collections.<MikuGrepMatch>emptyList(), ResultBuilder.createSummary(), diagnostics);
        }

        if (effectiveRequest.mode == RequestMode.LIST_FILES) {
            ListFilesResult listResult = ListFiles.runListFiles(effectiveRequest, rootCheck.realPath.toString(), diagnostics);
            MikuGrepResult result = ResultBuilder.finish(true, null, null, effectiveRequest,
                    Collections.<MikuGrepMatch>emptyList(), listResult.summary, diagnostics);
            result.files = listResult.files;
            result.fileSummary = listResult.fileSummary;
            return result;
        }

        SearchResult searchResult = Search.runSearch(effectiveRequest, rootCheck.realPath.toString(), diagnostics);
        MikuGrepResult result = ResultBuilder.finish(true, null, null, effectiveRequest, searchResult.matches, searchResult.summary, diagnostics);
        if (effectiveRequest.output.includeReadfileRequestHints.booleanValue()) {
            result.readfileHints = buildReadfileHints(effectiveRequest.root, searchResult.matches);
        }
        return result;
    }

    private static Path detectGitRootPath(Path startPath) {
        if (!Files.exists(startPath)) {
            return startPath;
        }
        Path current = startPath;
        if (!Files.isDirectory(current)) {
            current = current.getParent();
        }
        while (current != null) {
            if (Files.exists(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        return startPath;
    }

    private static String displayRoot(Path rootPath) {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path relative;
        try {
            relative = cwd.relativize(rootPath.toAbsolutePath().normalize());
        } catch (IllegalArgumentException ex) {
            return rootPath.toString().replace(java.io.File.separatorChar, '/');
        }
        String text = relative.toString();
        return text.length() == 0 ? "." : text.replace(java.io.File.separatorChar, '/');
    }

    private static List<ReadfileRequestHint> buildReadfileHints(String root, List<MikuGrepMatch> matches) {
        List<String> files = new ArrayList<String>();
        for (MikuGrepMatch match : matches) {
            String file = fileForHint(match);
            if (file != null && !files.contains(file)) {
                files.add(file);
            }
        }
        Collections.sort(files);
        List<ReadfileRequestHint> hints = new ArrayList<ReadfileRequestHint>();
        for (String file : files) {
            ReadfileRequestFile requestFile = new ReadfileRequestFile();
            requestFile.path = file;
            ReadfileRequest request = new ReadfileRequest();
            request.version = Integer.valueOf(1);
            request.root = root;
            request.files = Collections.singletonList(requestFile);
            ReadfileRequestHint hint = new ReadfileRequestHint();
            hint.file = file;
            hint.request = request;
            hints.add(hint);
        }
        return hints;
    }

    private static String fileForHint(MikuGrepMatch match) {
        if (match instanceof ContentMatch) {
            return ((ContentMatch) match).file;
        }
        if (match instanceof FilepathMatch) {
            return ((FilepathMatch) match).file;
        }
        if (match instanceof FileSummaryMatch) {
            return ((FileSummaryMatch) match).file;
        }
        if (match instanceof AgentFileMatch) {
            return ((AgentFileMatch) match).file;
        }
        return null;
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
