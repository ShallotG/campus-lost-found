package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchConfirmRequest {

    @NotNull(message = "拾物ID不能为空")
    private Long lostItemId;

    private Double matchScore;
}
