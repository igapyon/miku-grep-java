package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "path", "matchTypes", "directoryMatched", "matchCount", "relevance" })
public class DirectorySummaryMatch extends MikuGrepMatch {
    public String path;
    public List<MatchType> matchTypes;
    public Boolean directoryMatched;
    public Integer matchCount;
    public RelevanceInfo relevance;

    public DirectorySummaryMatch() {
        type = MatchType.DIRECTORY;
    }
}
