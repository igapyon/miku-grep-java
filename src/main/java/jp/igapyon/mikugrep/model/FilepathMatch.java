package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "type", "file", "matchedText" })
public class FilepathMatch extends MikuGrepMatch {
    public String file;
    public String matchedText;

    public FilepathMatch() {
        type = MatchType.FILEPATH;
    }
}
