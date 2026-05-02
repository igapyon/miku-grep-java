package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SupportedEncoding implements JsonValueEnum {
    UTF_8("utf-8"),
    SHIFT_JIS("shift_jis");

    private final String jsonValue;

    SupportedEncoding(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static SupportedEncoding fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(SupportedEncoding.class, value);
    }
}
