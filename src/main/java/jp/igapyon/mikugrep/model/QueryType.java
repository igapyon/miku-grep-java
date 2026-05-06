package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QueryType implements JsonValueEnum {
    LITERAL("literal"),
    REGEX("regex"),
    GLOB("glob");

    private final String jsonValue;

    QueryType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static QueryType fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(QueryType.class, value);
    }
}
