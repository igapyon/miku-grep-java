package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "pathPattern", "fileNamePattern", "encoding" })
public class EncodingRuleInput {
    public String pathPattern;
    public String fileNamePattern;
    public SupportedEncoding encoding;
}
