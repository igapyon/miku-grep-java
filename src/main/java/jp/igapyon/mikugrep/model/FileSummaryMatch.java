package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "file",
        "matchTypes",
        "filepathMatched",
        "contentMatched",
        "lines",
        "matchCount",
        "snippets",
        "encoding",
        "encodingRule"
})
public class FileSummaryMatch extends MikuGrepMatch {
    public String file;
    public List<MatchType> matchTypes;
    public Boolean filepathMatched;
    public Boolean contentMatched;
    public List<Integer> lines;
    public Integer matchCount;
    public List<FileSummarySnippet> snippets;
    public SupportedEncoding encoding;
    public EncodingRuleResult encodingRule;

    public FileSummaryMatch() {
        type = MatchType.FILE;
    }
}
