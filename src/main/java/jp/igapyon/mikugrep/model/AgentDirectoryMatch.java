package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "path", "targetKind", "matchTypes", "matchCount", "relevance" })
public class AgentDirectoryMatch extends MikuGrepMatch {
    public String path;
    public String targetKind;
    public List<MatchType> matchTypes;
    public Integer matchCount;
    public RelevanceInfo relevance;

    public AgentDirectoryMatch() {
        type = MatchType.AGENT_DIRECTORY;
        targetKind = "directory";
    }
}
