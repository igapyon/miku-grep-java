package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "line", "text", "trimmed", "textStartColumn" })
public class ContextLine {
    public Integer line;
    public String text;
    public Boolean trimmed;
    public Integer textStartColumn;
}
