package com.silliconthink.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silliconthink.user.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_post_tag")
public class BlogPostTagDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long tagId;
}
