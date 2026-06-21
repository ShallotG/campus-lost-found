package com.campus.lostfound.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClaimRecordResponse {

    private Long claimId;
    private Long lostItemId;
    private String itemCategory;
    private String itemImageUrl;
    private String claimerName;
    private String claimerIdentity;
    private String claimerPhone;
    private String handlerName;
    private String remark;
    private LocalDateTime claimTime;
}
