package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutputSort implements JsonValueEnum {
    PATH("path"),
    RELEVANCE("relevance");

    private final String jsonValue;

    OutputSort(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static OutputSort fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(OutputSort.class, value);
    }
}
