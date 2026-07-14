package com.silliconthink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private OAuth oauth = new OAuth();

    @Data
    public static class Jwt {
        private String secret;
        private long expireSeconds = 7200;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Data
    public static class OAuth {
        private String frontendCallbackUrl;
        private Github github = new Github();
    }

    @Data
    public static class Github {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizeUrl = "https://github.com/login/oauth/authorize";
        private String tokenUrl = "https://github.com/login/oauth/access_token";
        private String userApiUrl = "https://api.github.com/user";
    }
}
