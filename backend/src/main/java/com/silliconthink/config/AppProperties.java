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
    private Storage storage = new Storage();
    private Upload upload = new Upload();

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

    @Data
    public static class Storage {
        /**
         * Object-store root (local path or NAS mount).
         * Posts: {root}/posts/... ; media defaults under {root}/media when upload.dir empty.
         */
        private String root = "data/blog-storage";
        /** One-shot: export content_md rows to objects and fill content_key. */
        private boolean migrateOnStartup = false;
    }

    @Data
    public static class Upload {
        /**
         * Media directory. Empty = {storage.root}/media.
         * Prefer setting only BLOG_STORAGE_ROOT in production.
         */
        private String dir = "";
        /** URL path prefix served publicly, e.g. /uploads */
        private String publicPathPrefix = "/uploads";
        private long maxBytes = 5 * 1024 * 1024L;
    }
}
