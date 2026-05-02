package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "type", "file", "matchedText" })
public class FilenameMatch extends MikuGrepMatch {
    public String file;
    public String matchedText;

    public FilenameMatch() {
        type = MatchType.FILENAME;
    }
}
