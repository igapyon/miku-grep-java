package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "default", "rules", "onDecodeError" })
public class EncodingOptions {
    @JsonIgnore
    public SupportedEncoding defaultEncoding;
    public List<EncodingRuleInput> rules;
    public String onDecodeError;

    @com.fasterxml.jackson.annotation.JsonProperty("default")
    public SupportedEncoding getDefaultEncoding() {
        return defaultEncoding;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("default")
    public void setDefaultEncoding(SupportedEncoding defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }
}
