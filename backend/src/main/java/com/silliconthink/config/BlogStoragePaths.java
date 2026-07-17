package com.silliconthink.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class BlogStoragePaths {

    private final AppProperties appProperties;

    public Path storageRoot() {
        String root = appProperties.getStorage().getRoot();
        return Paths.get(StringUtils.hasText(root) ? root : "data/blog-storage")
                .toAbsolutePath()
                .normalize();
    }

    /** Absolute media directory used for uploads and static mapping. */
    public Path mediaDir() {
        String dir = appProperties.getUpload().getDir();
        if (StringUtils.hasText(dir)) {
            return Paths.get(dir).toAbsolutePath().normalize();
        }
        return storageRoot().resolve("media").normalize();
    }
}
