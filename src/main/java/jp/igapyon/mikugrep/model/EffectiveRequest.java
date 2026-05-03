package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "root", "query", "search", "output", "encoding", "ignore" })
public class EffectiveRequest {
    public String root;
    public Query query;
    public SearchOptions search;
    public OutputOptions output;
    public EncodingOptions encoding;
    public IgnoreOptions ignore;
}
