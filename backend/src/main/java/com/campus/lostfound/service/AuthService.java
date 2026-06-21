package com.campus.lostfound.service;

import com.campus.lostfound.dto.request.LoginRequest;
import com.campus.lostfound.dto.request.RefreshTokenRequest;
import com.campus.lostfound.dto.request.RegisterRequest;
import com.campus.lostfound.dto.response.LoginResponse;

public interface AuthService {

    /**
     * 失主注册
     */
    LoginResponse.UserInfo register(RegisterRequest request);

    /**
     * 统一登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新Token
     */
    LoginResponse refresh(RefreshTokenRequest request);

    /**
     * 登出
     */
    void logout(String token);
}
