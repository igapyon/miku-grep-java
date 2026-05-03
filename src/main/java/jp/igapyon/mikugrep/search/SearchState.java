package jp.igapyon.mikugrep.search;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.igapyon.mikugrep.model.Diagnostic;
import jp.igapyon.mikugrep.model.DirectorySummaryMatch;
import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.Summary;

final class SearchState {
    final EffectiveRequest request;
    final String rootPath;
    final Map<String, List<MikuGrepMatch>> detailsByFile = new LinkedHashMap<String, List<MikuGrepMatch>>();
    final Map<String, List<MikuGrepMatch>> detailsByDirectory = new LinkedHashMap<String, List<MikuGrepMatch>>();
    final Map<String, FileSummaryMatch> summariesByFile = new LinkedHashMap<String, FileSummaryMatch>();
    final Map<String, DirectorySummaryMatch> summariesByDirectory = new LinkedHashMap<String, DirectorySummaryMatch>();
    final List<Diagnostic> diagnostics;
    final Set<String> truncationDiagnosticKeys = new HashSet<String>();
    final Summary summary;
    int directoriesVisited;
    boolean globalLimitReached;

    SearchState(EffectiveRequest request, String rootPath, List<Diagnostic> diagnostics, Summary summary) {
        this.request = request;
        this.rootPath = rootPath;
        this.diagnostics = diagnostics;
        this.summary = summary;
    }
}
