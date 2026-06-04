package com.campus.lostfound.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostItemResponse {

    private Long id;
    private String imageUrl;
    private String category;
    private Double categoryConfidence;
    private String storageLocation;
    private String remark;
    private String status;
    private String createUserName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
