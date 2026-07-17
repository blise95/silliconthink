package com.silliconthink.blog.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silliconthink.blog.entity.BlogPostDO;
import com.silliconthink.blog.mapper.BlogPostMapper;
import com.silliconthink.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * One-shot: export legacy content_md into object store and fill content_key.
 * Enable with BLOG_MIGRATE_CONTENT=true (or app.storage.migrate-on-startup=true).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.storage", name = "migrate-on-startup", havingValue = "true")
public class BlogContentMigrationRunner implements ApplicationRunner {

    private final BlogPostMapper blogPostMapper;
    private final BlogObjectStore blogObjectStore;
    private final AppProperties appProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!blogObjectStore.isRootWritable()) {
            throw new IllegalStateException(
                    "Cannot migrate blog content: storage root not writable: " + blogObjectStore.rootPath());
        }
        List<BlogPostDO> posts = blogPostMapper.selectList(new LambdaQueryWrapper<BlogPostDO>()
                .and(w -> w.isNull(BlogPostDO::getContentKey).or().eq(BlogPostDO::getContentKey, ""))
                .isNotNull(BlogPostDO::getContentMd));
        int migrated = 0;
        for (BlogPostDO post : posts) {
            if (!StringUtils.hasText(post.getContentMd())) {
                continue;
            }
            String key = BlogObjectKeys.postContent(post.getAuthorId(), post.getId());
            blogObjectStore.putString(key, post.getContentMd());
            post.setContentKey(key);
            post.setContentMd("");
            blogPostMapper.updateById(post);
            migrated++;
        }
        log.info("Blog content migration finished: {} posts -> {}", migrated, appProperties.getStorage().getRoot());
    }
}
