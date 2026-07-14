package com.silliconthink.oauth;

import com.silliconthink.common.ErrorCode;
import com.silliconthink.config.AppProperties;
import com.silliconthink.exception.BizException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/oauth/github")
@RequiredArgsConstructor
public class GitHubOAuthController {

    private final GitHubOAuthService gitHubOAuthService;
    private final AppProperties appProperties;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(gitHubOAuthService.buildAuthorizeUrl());
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response
    ) throws IOException {
        String frontendCallback = appProperties.getOauth().getFrontendCallbackUrl();
        if (!StringUtils.hasText(frontendCallback)) {
            throw new BizException(ErrorCode.OAUTH_NOT_CONFIGURED, "frontend callback url is not configured");
        }
        if (StringUtils.hasText(error) || !StringUtils.hasText(code) || !StringUtils.hasText(state)) {
            response.sendRedirect(UriComponentsBuilder.fromUriString(frontendCallback)
                    .queryParam("error", error != null ? error : "oauth_failed")
                    .build(true)
                    .toUriString());
            return;
        }
        try {
            String oneTimeCode = gitHubOAuthService.handleCallback(code, state);
            response.sendRedirect(UriComponentsBuilder.fromUriString(frontendCallback)
                    .queryParam("code", oneTimeCode)
                    .build(true)
                    .toUriString());
        } catch (BizException ex) {
            response.sendRedirect(UriComponentsBuilder.fromUriString(frontendCallback)
                    .queryParam("error", ex.getMessage())
                    .build(true)
                    .toUriString());
        }
    }
}
