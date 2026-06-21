package com.campus.lostfound.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DetectResponse {

    private String category;

    @JsonProperty("category_cn")
    private String categoryCn;

    private Double confidence;

    private String description;

    private List<CategoryOption> top3;

    @Data
    public static class CategoryOption {
        private String category;

        @JsonProperty("category_cn")
        private String categoryCn;

        private Double confidence;
    }
}