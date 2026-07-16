package com.silliconthink.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silliconthink.blog.entity.BlogPostTagDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogPostTagMapper extends BaseMapper<BlogPostTagDO> {
}
