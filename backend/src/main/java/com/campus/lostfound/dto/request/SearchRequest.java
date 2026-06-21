package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchRequest {

    @NotBlank(message = "搜索描述不能为空")
    private String query;

    private Integer topK = 5;
}
