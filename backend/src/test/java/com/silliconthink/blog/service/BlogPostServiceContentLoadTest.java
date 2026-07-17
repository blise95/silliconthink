package com.silliconthink.blog.service;

import com.silliconthink.blog.entity.BlogPostDO;
import com.silliconthink.blog.storage.BlogObjectStore;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceContentLoadTest {

    @Mock
    BlogObjectStore blogObjectStore;

    @Test
    void loadContentPrefersObjectStore() {
        BlogPostService service = new BlogPostService(null, null, null, blogObjectStore);
        BlogPostDO post = new BlogPostDO();
        post.setContentKey("posts/1/1.md");
        post.setContentMd("legacy");
        when(blogObjectStore.getString("posts/1/1.md")).thenReturn(Optional.of("# from nas"));
        assertEquals("# from nas", service.loadContent(post));
    }

    @Test
    void loadContentFallsBackToLegacyColumn() {
        BlogPostService service = new BlogPostService(null, null, null, blogObjectStore);
        BlogPostDO post = new BlogPostDO();
        post.setContentKey(null);
        post.setContentMd("# legacy body");
        assertEquals("# legacy body", service.loadContent(post));
    }

    @Test
    void loadContentFallsBackWhenObjectMissingButLegacyPresent() {
        BlogPostService service = new BlogPostService(null, null, null, blogObjectStore);
        BlogPostDO post = new BlogPostDO();
        post.setId(9L);
        post.setContentKey("posts/1/9.md");
        post.setContentMd("# recovered");
        when(blogObjectStore.getString("posts/1/9.md")).thenReturn(Optional.empty());
        assertEquals("# recovered", service.loadContent(post));
    }

    @Test
    void loadContentMissingObjectAndLegacyThrows() {
        BlogPostService service = new BlogPostService(null, null, null, blogObjectStore);
        BlogPostDO post = new BlogPostDO();
        post.setContentKey("posts/1/9.md");
        post.setContentMd("");
        when(blogObjectStore.getString("posts/1/9.md")).thenReturn(Optional.empty());
        BizException ex = assertThrows(BizException.class, () -> service.loadContent(post));
        assertEquals(ErrorCode.CONTENT_OBJECT_MISSING.getCode(), ex.getCode());
    }
}
