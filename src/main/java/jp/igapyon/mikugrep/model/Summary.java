package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "filesVisited", "filesScanned", "filesMatched", "matches", "diagnostics", "truncated", "truncatedReason" })
public class Summary {
    public int filesVisited;
    public int filesScanned;
    public int filesMatched;
    public int matches;
    public int diagnostics;
    public boolean truncated;
    public String truncatedReason;
}
