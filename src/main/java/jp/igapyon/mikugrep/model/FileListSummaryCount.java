package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "extension", "path", "count" })
public class FileListSummaryCount {
    public String extension;
    public String path;
    public Integer count;
}
