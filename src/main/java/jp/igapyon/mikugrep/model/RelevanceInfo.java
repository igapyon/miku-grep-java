package jp.igapyon.mikugrep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "score", "reasons" })
public class RelevanceInfo {
    public Integer score;
    public List<String> reasons;
}
