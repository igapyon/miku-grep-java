package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "type", "path", "matchedText" })
public class DirectoryMatch extends MikuGrepMatch {
    public String path;
    public String matchedText;

    public DirectoryMatch() {
        type = MatchType.DIRECTORY;
    }
}
