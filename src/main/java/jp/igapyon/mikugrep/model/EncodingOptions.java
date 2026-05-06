package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "preset", "default", "rules", "onDecodeError" })
public class EncodingOptions {
    public EncodingPreset preset;
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
