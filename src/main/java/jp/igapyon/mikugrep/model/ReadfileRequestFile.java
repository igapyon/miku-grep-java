package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "path" })
public class ReadfileRequestFile {
    public String path;
}
