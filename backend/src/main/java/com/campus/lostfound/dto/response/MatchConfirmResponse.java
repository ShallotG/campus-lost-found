package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchConfirmResponse {

    private Long confirmId;
    private Long lostItemId;
    private Double matchScore;
    private LocalDateTime confirmTime;
    private ContactInfo contactInfo;

    // 物品关联信息
    private String itemImageUrl;
    private String itemCategory;
    private String itemStorageLocation;
    private String itemStatus;

    @Data
    @Builder
    public static class ContactInfo {
        private String address;
        private String phone;
        private String workingHours;
        private String claimNotice;
    }
}
