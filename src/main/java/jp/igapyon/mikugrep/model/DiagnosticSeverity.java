package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DiagnosticSeverity implements JsonValueEnum {
    ERROR("error"),
    WARNING("warning"),
    INFO("info");

    private final String jsonValue;

    DiagnosticSeverity(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static DiagnosticSeverity fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(DiagnosticSeverity.class, value);
    }
}
