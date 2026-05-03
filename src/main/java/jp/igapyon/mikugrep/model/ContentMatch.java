package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "file",
        "line",
        "column",
        "matchedText",
        "text",
        "trimmed",
        "textStartColumn",
        "contextBefore",
        "contextAfter",
        "encoding",
        "encodingRule"
})
public class ContentMatch extends MikuGrepMatch {
    public String file;
    public Integer line;
    public Integer column;
    public String matchedText;
    public String text;
    public Boolean trimmed;
    public Integer textStartColumn;
    public java.util.List<ContextLine> contextBefore;
    public java.util.List<ContextLine> contextAfter;
    public SupportedEncoding encoding;
    public EncodingRuleResult encodingRule;

    public ContentMatch() {
        type = MatchType.CONTENT;
    }
}
