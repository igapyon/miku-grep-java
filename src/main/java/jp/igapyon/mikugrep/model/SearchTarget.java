package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchTarget implements JsonValueEnum {
    CONTENT("content"),
    FILENAME("filename"),
    BOTH("both");

    private final String jsonValue;

    SearchTarget(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static SearchTarget fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(SearchTarget.class, value);
    }
}
