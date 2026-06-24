package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FoundItemResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String itemName;
    private String category;
    private String color;
    private String brand;
    private String description;
    private LocalDateTime lostTime;
    private String lostLocation;
    private String contactPhone;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
