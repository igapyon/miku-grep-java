package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "requestedRoot", "root", "detectGitRoot", "mode", "query", "search", "output", "encoding", "ignore" })
public class EffectiveRequest {
    public String requestedRoot;
    public String root;
    public Boolean detectGitRoot;
    public RequestMode mode;
    public Query query;
    public SearchOptions search;
    public OutputOptions output;
    public EncodingOptions encoding;
    public IgnoreOptions ignore;
}
