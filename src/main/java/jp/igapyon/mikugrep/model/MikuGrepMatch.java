package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilepathMatch.class, name = "filepath"),
        @JsonSubTypes.Type(value = DirectoryMatch.class, name = "directory"),
        @JsonSubTypes.Type(value = ContentMatch.class, name = "content"),
        @JsonSubTypes.Type(value = FileSummaryMatch.class, name = "file"),
        @JsonSubTypes.Type(value = DirectorySummaryMatch.class, name = "directory"),
        @JsonSubTypes.Type(value = AgentFileMatch.class, name = "agentFile"),
        @JsonSubTypes.Type(value = AgentDirectoryMatch.class, name = "agentDirectory")
})
public abstract class MikuGrepMatch {
    public MatchType type;
}
