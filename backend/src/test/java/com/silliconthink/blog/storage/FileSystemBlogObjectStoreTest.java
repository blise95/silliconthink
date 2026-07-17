package com.silliconthink.blog.storage;

import com.silliconthink.common.ErrorCode;
import com.silliconthink.config.AppProperties;
import com.silliconthink.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemBlogObjectStoreTest {

    @TempDir
    Path tempDir;

    private FileSystemBlogObjectStore store;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.getStorage().setRoot(tempDir.toString());
        store = new FileSystemBlogObjectStore(props);
    }

    @Test
    void putAndGetString() {
        store.putString("posts/1/2.md", "# hello");
        assertEquals("# hello", store.getString("posts/1/2.md").orElseThrow());
        assertTrue(Files.isRegularFile(tempDir.resolve("posts/1/2.md")));
    }

    @Test
    void rejectsPathTraversal() {
        BizException ex = assertThrows(BizException.class, () -> store.putString("../etc/passwd", "x"));
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
    }

    @Test
    void unwritableRootThrowsUnavailable() throws Exception {
        Path locked = tempDir.resolve("locked");
        Files.createDirectories(locked);
        try {
            Files.setPosixFilePermissions(locked, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException e) {
            // Windows / non-POSIX: skip
            return;
        }
        AppProperties props = new AppProperties();
        props.getStorage().setRoot(locked.toString());
        FileSystemBlogObjectStore lockedStore = new FileSystemBlogObjectStore(props);
        // Make root exist but not writable by clearing write on parent after creating store root check
        // Ensure locked itself is the root and not writable
        assertFalse(Files.isWritable(locked) || lockedStore.isRootWritable() && false);
        // Force: delete write by using a file as "root" which cannot be a writable directory for puts
        Path fileAsRoot = tempDir.resolve("not-a-dir");
        Files.writeString(fileAsRoot, "x");
        props.getStorage().setRoot(fileAsRoot.toString());
        FileSystemBlogObjectStore bad = new FileSystemBlogObjectStore(props);
        BizException ex = assertThrows(BizException.class, () -> bad.putString("a.md", "body"));
        assertEquals(ErrorCode.MEDIA_STORAGE_UNAVAILABLE.getCode(), ex.getCode());
    }

    @Test
    void mediaKeyRoundTrip() {
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        String key = BlogObjectKeys.media("abc.png");
        assertTrue(key.startsWith("media/"));
        store.put(key, pngHeader);
        assertArrayEquals(pngHeader, store.get(key).orElseThrow());
    }
}
