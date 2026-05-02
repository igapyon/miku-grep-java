package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutputMode implements JsonValueEnum {
    DETAIL("detail"),
    FILE_SUMMARY("file-summary");

    private final String jsonValue;

    OutputMode(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static OutputMode fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(OutputMode.class, value);
    }
}
