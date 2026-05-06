package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EncodingRuleType implements JsonValueEnum {
    PATH_PATTERN("pathPattern"),
    FILE_NAME_PATTERN("fileNamePattern"),
    PRESET("preset"),
    DEFAULT("default");

    private final String jsonValue;

    EncodingRuleType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static EncodingRuleType fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(EncodingRuleType.class, value);
    }
}
