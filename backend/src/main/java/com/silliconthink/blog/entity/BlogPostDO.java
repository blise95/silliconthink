package com.silliconthink.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silliconthink.user.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_post")
public class BlogPostDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long authorId;

    private String title;

    private String slug;

    private String summary;

    private String contentMd;

    private String coverUrl;

    /** draft | published */
    private String status;

    private LocalDateTime publishedAt;

    @TableLogic
    private Integer deleted;
}
