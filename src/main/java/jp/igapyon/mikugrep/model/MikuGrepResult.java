package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "ok", "error", "effectiveRequest", "matches", "summary", "diagnostics" })
public class MikuGrepResult {
    public int version;
    public boolean ok;
    public MikuGrepError error;
    public Object effectiveRequest;
    public List<MikuGrepMatch> matches;
    public Summary summary;
    public List<Diagnostic> diagnostics;
}
