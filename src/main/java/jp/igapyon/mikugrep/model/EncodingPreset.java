package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EncodingPreset implements JsonValueEnum {
    JAPANESE_LEGACY("japanese-legacy");

    private final String jsonValue;

    EncodingPreset(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static EncodingPreset fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(EncodingPreset.class, value);
    }
}
