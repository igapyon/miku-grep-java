package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "ok", "error", "effectiveRequest", "matches", "files", "fileSummary", "readfileHints", "summary", "diagnostics" })
public class MikuGrepResult {
    public int version;
    public boolean ok;
    public MikuGrepError error;
    public Object effectiveRequest;
    public List<MikuGrepMatch> matches;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<FileListEntry> files;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public FileListSummary fileSummary;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<ReadfileRequestHint> readfileHints;
    public Summary summary;
    public List<Diagnostic> diagnostics;
}
