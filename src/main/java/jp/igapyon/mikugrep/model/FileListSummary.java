package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "files", "extensions", "directories" })
public class FileListSummary {
    public Integer files;
    public List<FileListSummaryCount> extensions;
    public List<FileListSummaryCount> directories;
}
