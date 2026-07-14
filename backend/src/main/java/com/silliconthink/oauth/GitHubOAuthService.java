package com.silliconthink.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.common.SystemConstants;
import com.silliconthink.config.AppProperties;
import com.silliconthink.exception.BizException;
import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.entity.UserOauthDO;
import com.silliconthink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GitHubOAuthService {

    private final AppProperties appProperties;
    private final OAuthTicketStore ticketStore;
    private final UserService userService;
    private final RestClient.Builder restClientBuilder;

    public String buildAuthorizeUrl() {
        AppProperties.Github github = appProperties.getOauth().getGithub();
        ensureConfigured(github);
        String state = ticketStore.createState();
        return UriComponentsBuilder.fromUriString(github.getAuthorizeUrl())
                .queryParam("client_id", github.getClientId())
                .queryParam("redirect_uri", github.getRedirectUri())
                .queryParam("scope", "read:user")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    @Transactional(rollbackFor = Exception.class)
    public String handleCallback(String code, String state) {
        AppProperties.Github github = appProperties.getOauth().getGithub();
        ensureConfigured(github);
        if (!ticketStore.consumeState(state)) {
            throw new BizException(ErrorCode.OAUTH_STATE_INVALID);
        }
        String accessToken = exchangeAccessToken(github, code);
        GitHubUser ghUser = fetchUser(github, accessToken);
        UserOauthDO binding = userService.findOauthBinding(SystemConstants.PROVIDER_GITHUB, ghUser.id());
        UserDO user;
        if (binding != null) {
            user = userService.findById(binding.getUserId());
            userService.assertEnabled(user);
        } else {
            String login = sanitizeUsername(ghUser.login());
            String displayName = StringUtils.hasText(ghUser.name()) ? ghUser.name() : ghUser.login();
            user = userService.createOAuthUser(login, displayName);
            userService.bindOauth(user.getId(), SystemConstants.PROVIDER_GITHUB, ghUser.id(), ghUser.login());
        }
        return ticketStore.createExchangeCode(user.getId());
    }

    public Long consumeExchangeCode(String code) {
        return ticketStore.consumeExchangeCode(code)
                .orElseThrow(() -> new BizException(ErrorCode.OAUTH_EXCHANGE_INVALID));
    }

    private void ensureConfigured(AppProperties.Github github) {
        if (!StringUtils.hasText(github.getClientId()) || !StringUtils.hasText(github.getClientSecret())) {
            throw new BizException(ErrorCode.OAUTH_NOT_CONFIGURED);
        }
    }

    private String exchangeAccessToken(AppProperties.Github github, String code) {
        try {
            JsonNode node = restClientBuilder.build()
                    .post()
                    .uri(github.getTokenUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(MapOf.of(
                            "client_id", github.getClientId(),
                            "client_secret", github.getClientSecret(),
                            "code", code,
                            "redirect_uri", github.getRedirectUri()
                    ))
                    .retrieve()
                    .body(JsonNode.class);
            if (node == null || !node.hasNonNull("access_token")) {
                throw new BizException(ErrorCode.OAUTH_PROVIDER_ERROR);
            }
            return node.get("access_token").asText();
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(ErrorCode.OAUTH_PROVIDER_ERROR, "failed to exchange github token");
        }
    }

    private GitHubUser fetchUser(AppProperties.Github github, String accessToken) {
        try {
            JsonNode node = restClientBuilder.build()
                    .get()
                    .uri(github.getUserApiUrl())
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            if (node == null || !node.hasNonNull("id")) {
                throw new BizException(ErrorCode.OAUTH_PROVIDER_ERROR);
            }
            String id = node.get("id").asText();
            String login = node.path("login").asText("user");
            String name = node.path("name").isNull() ? null : node.path("name").asText(null);
            return new GitHubUser(id, login, name);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(ErrorCode.OAUTH_PROVIDER_ERROR, "failed to fetch github user");
        }
    }

    private String sanitizeUsername(String login) {
        String cleaned = login == null ? "github_user" : login.replaceAll("[^a-zA-Z0-9_]", "_");
        if (cleaned.length() < 4) {
            cleaned = cleaned + "_gh";
        }
        if (cleaned.length() > 28) {
            cleaned = cleaned.substring(0, 28);
        }
        return cleaned;
    }

    private record GitHubUser(String id, String login, String name) {
    }

    /** tiny helper to avoid bringing Guava */
    private static final class MapOf {
        static java.util.Map<String, String> of(String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4) {
            return java.util.Map.of(k1, v1, k2, v2, k3, v3, k4, v4);
        }
    }
}
