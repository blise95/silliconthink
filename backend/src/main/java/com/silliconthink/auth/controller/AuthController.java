package com.silliconthink.auth.controller;

import com.silliconthink.auth.dto.LoginRequest;
import com.silliconthink.auth.dto.OAuthExchangeRequest;
import com.silliconthink.auth.dto.RegisterRequest;
import com.silliconthink.auth.dto.TokenResponse;
import com.silliconthink.auth.dto.UserInfoVO;
import com.silliconthink.auth.security.AuthUser;
import com.silliconthink.auth.service.AuthService;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.common.Result;
import com.silliconthink.exception.BizException;
import com.silliconthink.oauth.GitHubOAuthService;
import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GitHubOAuthService gitHubOAuthService;
    private final UserService userService;

    @PostMapping("/register")
    public Result<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return Result.ok(authService.me(authUser.getUserId()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return Result.ok();
    }

    @PostMapping("/oauth/exchange")
    public Result<TokenResponse> exchange(@Valid @RequestBody OAuthExchangeRequest request) {
        Long userId = gitHubOAuthService.consumeExchangeCode(request.getCode());
        UserDO user = userService.findById(userId);
        userService.assertEnabled(user);
        return Result.ok(authService.issueToken(user));
    }
}
