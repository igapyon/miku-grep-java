package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "pattern", "preset" })
public class EncodingRuleResult {
    public EncodingRuleType type;
    public String pattern;
    public EncodingPreset preset;
}
