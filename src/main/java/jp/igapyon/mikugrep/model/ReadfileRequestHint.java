package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "file", "request" })
public class ReadfileRequestHint {
    public String file;
    public ReadfileRequest request;
}
