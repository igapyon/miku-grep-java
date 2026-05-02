package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "target",
        "recursive",
        "maxDepth",
        "maxFileBytes",
        "maxLineChars",
        "maxFilesVisited",
        "maxDirectoriesVisited",
        "includeFileNamePatterns",
        "excludeFileNamePatterns",
        "excludeDirNamePatterns"
})
public class SearchOptions {
    public SearchTarget target;
    public Boolean recursive;
    public Integer maxDepth;
    public Long maxFileBytes;
    public Integer maxLineChars;
    public Integer maxFilesVisited;
    public Integer maxDirectoriesVisited;
    public List<String> includeFileNamePatterns;
    public List<String> excludeFileNamePatterns;
    public List<String> excludeDirNamePatterns;
}
