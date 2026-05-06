package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "text", "case" })
public class Query {
    public QueryType type;
    public String text;
    @com.fasterxml.jackson.annotation.JsonProperty("case")
    public QueryCase queryCase;
}
