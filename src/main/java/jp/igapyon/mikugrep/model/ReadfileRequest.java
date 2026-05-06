package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "root", "files" })
public class ReadfileRequest {
    public Integer version;
    public String root;
    public List<ReadfileRequestFile> files;
}
