package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchResultResponse {

    private List<SearchResultItem> results;
    private long totalCompared;
    private long queryTime;

    @Data
    @Builder
    public static class SearchResultItem {
        private Long id;
        private String imageUrl;
        private String category;
        private String storageLocation;
        private String remark;
        private LocalDateTime createTime;
        private Double matchScore;
        private String explanation;
    }
}
