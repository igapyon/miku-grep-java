package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "filesVisited",
        "directoriesVisited",
        "filesScanned",
        "directoriesScanned",
        "filesMatched",
        "directoriesMatched",
        "filesIgnored",
        "directoriesIgnored",
        "matches",
        "diagnostics",
        "truncated",
        "truncatedReason"
})
public class Summary {
    public int filesVisited;
    public int directoriesVisited;
    public int filesScanned;
    public int directoriesScanned;
    public int filesMatched;
    public int directoriesMatched;
    public int filesIgnored;
    public int directoriesIgnored;
    public int matches;
    public int diagnostics;
    public boolean truncated;
    public String truncatedReason;
}
