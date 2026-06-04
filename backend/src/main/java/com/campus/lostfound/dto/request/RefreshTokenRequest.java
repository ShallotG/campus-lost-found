package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "RefreshToken不能为空")
    private String refreshToken;
}
