package com.silliconthink.auth.service.impl;

import com.silliconthink.auth.dto.LoginRequest;
import com.silliconthink.auth.dto.RegisterRequest;
import com.silliconthink.auth.dto.TokenResponse;
import com.silliconthink.auth.dto.UserInfoVO;
import com.silliconthink.auth.security.JwtTokenProvider;
import com.silliconthink.auth.service.AuthService;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.exception.BizException;
import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenResponse register(RegisterRequest request) {
        if (!isStrongEnough(request.getPassword())) {
            throw new BizException(ErrorCode.WEAK_PASSWORD);
        }
        String displayName = StringUtils.hasText(request.getDisplayName())
                ? request.getDisplayName().trim()
                : request.getUsername();
        String hash = passwordEncoder.encode(request.getPassword());
        UserDO user = userService.createLocalUser(request.getUsername().trim(), hash, displayName);
        return issueToken(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        UserDO user = userService.findByUsername(request.getUsername().trim());
        if (user == null) {
            throw new BizException(ErrorCode.AUTH_FAILED);
        }
        userService.assertEnabled(user);
        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new BizException(ErrorCode.PASSWORD_LOGIN_UNSUPPORTED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.AUTH_FAILED);
        }
        return issueToken(user);
    }

    @Override
    public UserInfoVO me(Long userId) {
        UserDO user = userService.findById(userId);
        userService.assertEnabled(user);
        return toUserInfo(user);
    }

    @Override
    public TokenResponse issueToken(UserDO user) {
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        return TokenResponse.of(token, toUserInfo(user));
    }

    private UserInfoVO toUserInfo(UserDO user) {
        return new UserInfoVO(user.getId(), user.getUsername(), user.getDisplayName());
    }

    private boolean isStrongEnough(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
}
