package jp.igapyon.mikugrep.search;

import java.util.List;

import jp.igapyon.mikugrep.model.MikuGrepMatch;
import jp.igapyon.mikugrep.model.Summary;

public final class SearchResult {
    public final List<MikuGrepMatch> matches;
    public final Summary summary;

    public SearchResult(List<MikuGrepMatch> matches, Summary summary) {
        this.matches = matches;
        this.summary = summary;
    }
}
