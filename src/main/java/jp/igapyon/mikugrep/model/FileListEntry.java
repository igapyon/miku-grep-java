package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "path", "extension", "directory" })
public class FileListEntry {
    public String path;
    public String extension;
    public String directory;
}
