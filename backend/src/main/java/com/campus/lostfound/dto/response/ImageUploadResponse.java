package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImageUploadResponse {

    private String imageUrl;
    private String category;
    private String categoryCn;
    private Double confidence;
    /** AI 生成的细致描述（颜色+类别，如"白色水杯"） */
    private String description;
    /** Top3 候选类别（供工作人员选择） */
    private List<CategoryOption> top3;

    @Data
    @Builder
    public static class CategoryOption {
        private String category;
        private String categoryCn;
        private Double confidence;
    }
}
