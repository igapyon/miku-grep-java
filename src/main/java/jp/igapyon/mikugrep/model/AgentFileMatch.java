package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "file",
        "targetKind",
        "matchTypes",
        "matchCount",
        "lines",
        "representativeSnippets",
        "readRanges",
        "relevance",
        "encoding",
        "encodingRule"
})
public class AgentFileMatch extends MikuGrepMatch {
    public String file;
    public String targetKind;
    public List<MatchType> matchTypes;
    public Integer matchCount;
    public List<Integer> lines;
    public List<FileSummarySnippet> representativeSnippets;
    public List<ReadRangeCandidate> readRanges;
    public RelevanceInfo relevance;
    public SupportedEncoding encoding;
    public EncodingRuleResult encodingRule;

    public AgentFileMatch() {
        type = MatchType.AGENT_FILE;
        targetKind = "file";
    }
}
