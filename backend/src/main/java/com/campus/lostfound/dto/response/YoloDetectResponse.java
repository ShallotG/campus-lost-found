package com.campus.lostfound.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YoloDetectResponse {

    private String category;

    /** Flask 返回 snake_case 字段名 "category_cn"，需显式映射 */
    @JsonProperty("category_cn")
    private String categoryCn;

    private Double confidence;

    /** AI 生成的细致描述（颜色+类别，如"白色水杯"） */
    private String description;

    /** Top3 候选类别 */
    private List<CategoryOption> top3;

    @Data
    public static class CategoryOption {
        private String category;

        /** Flask 返回 snake_case 字段名 "category_cn"，需显式映射 */
        @JsonProperty("category_cn")
        private String categoryCn;

        private Double confidence;
    }
}
