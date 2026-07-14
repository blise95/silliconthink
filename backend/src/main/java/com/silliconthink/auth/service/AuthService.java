package com.silliconthink.auth.service;

import com.silliconthink.auth.dto.LoginRequest;
import com.silliconthink.auth.dto.RegisterRequest;
import com.silliconthink.auth.dto.TokenResponse;
import com.silliconthink.auth.dto.UserInfoVO;
import com.silliconthink.user.entity.UserDO;

public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    UserInfoVO me(Long userId);

    TokenResponse issueToken(UserDO user);
}
