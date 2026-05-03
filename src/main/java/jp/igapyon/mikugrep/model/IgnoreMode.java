package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IgnoreMode implements JsonValueEnum {
    AUTO("auto"),
    NONE("none");

    private final String jsonValue;

    IgnoreMode(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static IgnoreMode fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(IgnoreMode.class, value);
    }
}
