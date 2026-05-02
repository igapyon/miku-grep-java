package jp.igapyon.mikugrep.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "severity",
        "code",
        "message",
        "file",
        "path",
        "line",
        "skipped",
        "encoding",
        "encodingRule",
        "details"
})
public class Diagnostic {
    public DiagnosticSeverity severity;
    public String code;
    public String message;
    public String file;
    public String path;
    public Integer line;
    public Boolean skipped;
    public SupportedEncoding encoding;
    public EncodingRuleResult encodingRule;
    public Map<String, Object> details;
}
