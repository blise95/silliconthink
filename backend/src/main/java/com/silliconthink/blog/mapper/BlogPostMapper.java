package com.silliconthink.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silliconthink.blog.entity.BlogPostDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BlogPostMapper extends BaseMapper<BlogPostDO> {

    /**
     * 释放已软删行上的 slug，腾出 {@code uk_blog_post_slug} 供新文章复用。
     * 原生 SQL 不受 {@code @TableLogic} 过滤影响。
     */
    @Update("""
            UPDATE blog_post
            SET slug = CONCAT('__del_', id)
            WHERE deleted = 1
              AND slug = #{slug}
              AND (#{excludeId} IS NULL OR id <> #{excludeId})
            """)
    int releaseDeletedSlug(@Param("slug") String slug, @Param("excludeId") Long excludeId);
}
