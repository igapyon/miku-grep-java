package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "mode", "maxMatches", "maxMatchesPerFile", "maxLineLength", "maxSnippetsPerFile" })
public class OutputOptions {
    public OutputMode mode;
    public Integer maxMatches;
    public Integer maxMatchesPerFile;
    public Integer maxLineLength;
    public Integer maxSnippetsPerFile;
}
