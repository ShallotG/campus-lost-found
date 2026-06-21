package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimRequest {

    @NotNull(message = "拾物ID不能为空")
    private Long lostItemId;

    @NotBlank(message = "领取人姓名不能为空")
    private String claimerName;

    @NotBlank(message = "领取人学号/工号不能为空")
    private String claimerIdentity;

    private String claimerPhone;

    private String remark;
}
