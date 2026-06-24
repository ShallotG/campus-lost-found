package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "姓名长度不能超过50")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.\\w+$", message = "邮箱格式不正确")
    private String email;
}