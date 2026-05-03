package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "mode", "sources", "useGlobalGitignore", "loadedSources" })
public class IgnoreOptions {
    public IgnoreMode mode;
    public List<String> sources;
    public Boolean useGlobalGitignore;
    public List<IgnoreLoadedSource> loadedSources;
}
