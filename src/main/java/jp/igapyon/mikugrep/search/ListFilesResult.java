package jp.igapyon.mikugrep.search;

import java.util.List;

import jp.igapyon.mikugrep.model.FileListEntry;
import jp.igapyon.mikugrep.model.FileListSummary;
import jp.igapyon.mikugrep.model.Summary;

public final class ListFilesResult {
    public final List<FileListEntry> files;
    public final FileListSummary fileSummary;
    public final Summary summary;

    public ListFilesResult(List<FileListEntry> files, FileListSummary fileSummary, Summary summary) {
        this.files = files;
        this.fileSummary = fileSummary;
        this.summary = summary;
    }
}
