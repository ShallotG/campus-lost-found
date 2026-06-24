package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoundItemCreateRequest {

    @NotBlank(message = "物品名称不能为空")
    private String itemName;

    private String category;

    private String color;

    private String brand;

    private String description;

    private LocalDateTime lostTime;

    private String lostLocation;

    private String contactPhone;
}
