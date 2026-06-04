package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchConfirmResponse {

    private Long confirmId;
    private ContactInfo contactInfo;

    @Data
    @Builder
    public static class ContactInfo {
        private String address;
        private String phone;
        private String workingHours;
        private String claimNotice;
    }
}
