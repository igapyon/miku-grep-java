package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QueryCase implements JsonValueEnum {
    SENSITIVE("sensitive"),
    INSENSITIVE("insensitive");

    private final String jsonValue;

    QueryCase(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static QueryCase fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(QueryCase.class, value);
    }
}
