package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "line", "text", "trimmed", "textStartColumn" })
public class FileSummarySnippet {
    public MatchType type;
    public Integer line;
    public String text;
    public Boolean trimmed;
    public Integer textStartColumn;

    public FileSummarySnippet() {
        type = MatchType.CONTENT;
    }
}
