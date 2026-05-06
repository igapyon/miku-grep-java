package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "mode", "sort", "maxMatches", "maxMatchesPerFile", "maxLineLength", "maxSnippetsPerFile", "includeReadfileRequestHints", "contextLinesBefore", "contextLinesAfter" })
public class OutputOptions {
    public OutputMode mode;
    public OutputSort sort;
    public Integer maxMatches;
    public Integer maxMatchesPerFile;
    public Integer maxLineLength;
    public Integer maxSnippetsPerFile;
    public Boolean includeReadfileRequestHints;
    public Integer contextLinesBefore;
    public Integer contextLinesAfter;
}
