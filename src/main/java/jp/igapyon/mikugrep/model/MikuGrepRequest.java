package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "version", "root", "query", "search", "output", "encoding" })
public class MikuGrepRequest {
    public int version;
    public String root;
    public Query query;
    public SearchOptions search;
    public OutputOptions output;
    public EncodingOptions encoding;
}
