package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilenameMatch.class, name = "filename"),
        @JsonSubTypes.Type(value = ContentMatch.class, name = "content"),
        @JsonSubTypes.Type(value = FileSummaryMatch.class, name = "file")
})
public abstract class MikuGrepMatch {
    public MatchType type;
}
