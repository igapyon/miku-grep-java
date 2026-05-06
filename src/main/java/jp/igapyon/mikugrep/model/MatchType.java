package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MatchType implements JsonValueEnum {
    FILEPATH("filepath"),
    DIRECTORY("directory"),
    CONTENT("content"),
    FILE("file"),
    AGENT_FILE("agentFile"),
    AGENT_DIRECTORY("agentDirectory");

    private final String jsonValue;

    MatchType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static MatchType fromJsonValue(String value) {
        return JsonEnum.fromJsonValue(MatchType.class, value);
    }
}
