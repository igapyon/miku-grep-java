package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "type", "path", "matchTypes", "directoryMatched", "matchCount" })
public class DirectorySummaryMatch extends MikuGrepMatch {
    public String path;
    public List<MatchType> matchTypes;
    public Boolean directoryMatched;
    public Integer matchCount;

    public DirectorySummaryMatch() {
        type = MatchType.DIRECTORY;
    }
}
