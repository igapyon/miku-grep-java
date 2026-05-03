package jp.igapyon.mikugrep.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "path", "baseDirectory", "patterns", "unsupportedPatterns" })
public class IgnoreLoadedSource {
    public String path;
    public String baseDirectory;
    public Integer patterns;
    public Integer unsupportedPatterns;
}
