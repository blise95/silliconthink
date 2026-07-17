package com.silliconthink.blog.storage;

import com.silliconthink.common.ErrorCode;
import com.silliconthink.config.AppProperties;
import com.silliconthink.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
public class FileSystemBlogObjectStore implements BlogObjectStore {

    private final Path root;

    public FileSystemBlogObjectStore(AppProperties appProperties) {
        String configured = appProperties.getStorage().getRoot();
        this.root = Paths.get(StringUtils.hasText(configured) ? configured : "data/blog-storage")
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public void put(String key, byte[] data) {
        ensureWritable();
        Path target = resolveKey(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, data);
        } catch (IOException e) {
            log.error("Failed to write object key={}", key, e);
            throw new BizException(ErrorCode.MEDIA_STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public void putString(String key, String utf8Text) {
        put(key, (utf8Text == null ? "" : utf8Text).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<byte[]> get(String key) {
        Path target = resolveKey(key);
        if (!Files.isRegularFile(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException e) {
            log.error("Failed to read object key={}", key, e);
            throw new BizException(ErrorCode.MEDIA_STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public Optional<String> getString(String key) {
        return get(key).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public void delete(String key) {
        Path target = resolveKey(key);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Failed to delete object key={}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.isRegularFile(resolveKey(key));
    }

    @Override
    public boolean isRootWritable() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            return Files.isDirectory(root) && Files.isWritable(root);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Path rootPath() {
        return root;
    }

    private void ensureWritable() {
        if (!isRootWritable()) {
            throw new BizException(ErrorCode.MEDIA_STORAGE_UNAVAILABLE);
        }
    }

    private Path resolveKey(String key) {
        if (!StringUtils.hasText(key) || key.contains("..") || key.startsWith("/") || key.startsWith("\\")) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return resolved;
    }
}
