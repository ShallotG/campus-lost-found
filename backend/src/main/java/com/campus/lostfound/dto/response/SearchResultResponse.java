package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchResultResponse {

    private List<SearchResultItem> results;
    private int totalCompared;
    private long queryTime;
    private long total;
    private long page;
    private long pages;

    @Data
    @Builder
    public static class SearchResultItem {
        private Long id;
        private String imageUrl;
        private String category;
        private String storageLocation;
        private String remark;
        private Double matchScore;
        private String explanation;
        private LocalDateTime createTime;
    }
}
