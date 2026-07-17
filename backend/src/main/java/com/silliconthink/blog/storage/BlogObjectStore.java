package com.silliconthink.blog.storage;

import java.util.Optional;

/**
 * Filesystem-backed object store (local dir or NAS mount), OSS-like API.
 */
public interface BlogObjectStore {

    void put(String key, byte[] data);

    void putString(String key, String utf8Text);

    Optional<byte[]> get(String key);

    Optional<String> getString(String key);

    void delete(String key);

    boolean exists(String key);

    /** True when the storage root exists and is writable by this process. */
    boolean isRootWritable();

    /** Absolute path of the storage root (for resource handlers / diagnostics). */
    java.nio.file.Path rootPath();
}
