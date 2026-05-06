package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "startLine", "endLine", "reason" })
public class ReadRangeCandidate {
    public Integer startLine;
    public Integer endLine;
    public String reason;
}
