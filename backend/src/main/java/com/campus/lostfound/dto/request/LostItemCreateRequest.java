package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LostItemCreateRequest {

    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    @NotBlank(message = "物品类别不能为空")
    private String category;

    private Double categoryConfidence;

    @NotBlank(message = "存放位置不能为空")
    private String storageLocation;

    private String remark;
}
