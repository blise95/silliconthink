package com.silliconthink.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silliconthink.blog.entity.BlogPostTagDO;
import com.silliconthink.blog.entity.BlogTagDO;
import com.silliconthink.blog.mapper.BlogPostTagMapper;
import com.silliconthink.blog.mapper.BlogTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogTagService {

    private final BlogTagMapper blogTagMapper;
    private final BlogPostTagMapper blogPostTagMapper;

    public List<String> listTagNamesByPostId(Long postId) {
        List<BlogPostTagDO> links = blogPostTagMapper.selectList(
                new LambdaQueryWrapper<BlogPostTagDO>().eq(BlogPostTagDO::getPostId, postId));
        if (links.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = links.stream().map(BlogPostTagDO::getTagId).toList();
        List<BlogTagDO> tags = blogTagMapper.selectBatchIds(tagIds);
        Map<Long, String> nameById = tags.stream()
                .collect(Collectors.toMap(BlogTagDO::getId, BlogTagDO::getName, (a, b) -> a));
        return tagIds.stream().map(nameById::get).filter(Objects::nonNull).toList();
    }

    public Map<Long, List<String>> listTagNamesByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        List<BlogPostTagDO> links = blogPostTagMapper.selectList(
                new LambdaQueryWrapper<BlogPostTagDO>().in(BlogPostTagDO::getPostId, postIds));
        if (links.isEmpty()) {
            return postIds.stream().collect(Collectors.toMap(id -> id, id -> List.of()));
        }
        List<Long> tagIds = links.stream().map(BlogPostTagDO::getTagId).distinct().toList();
        Map<Long, String> nameById = blogTagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(BlogTagDO::getId, BlogTagDO::getName, (a, b) -> a));
        Map<Long, List<String>> result = postIds.stream()
                .collect(Collectors.toMap(id -> id, id -> new ArrayList<>()));
        for (BlogPostTagDO link : links) {
            String name = nameById.get(link.getTagId());
            if (name != null) {
                result.computeIfAbsent(link.getPostId(), k -> new ArrayList<>()).add(name);
            }
        }
        return result;
    }

    @Transactional
    public void replacePostTags(Long postId, List<String> tagNames) {
        blogPostTagMapper.delete(new LambdaQueryWrapper<BlogPostTagDO>().eq(BlogPostTagDO::getPostId, postId));
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : tagNames) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String name = raw.trim();
            if (name.length() > 64) {
                name = name.substring(0, 64);
            }
            normalized.add(name);
        }
        for (String name : normalized) {
            BlogTagDO tag = findOrCreateTag(name);
            BlogPostTagDO link = new BlogPostTagDO();
            link.setPostId(postId);
            link.setTagId(tag.getId());
            blogPostTagMapper.insert(link);
        }
    }

    private BlogTagDO findOrCreateTag(String name) {
        BlogTagDO existing = blogTagMapper.selectOne(
                new LambdaQueryWrapper<BlogTagDO>().eq(BlogTagDO::getName, name).last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        String slug = toSlug(name);
        BlogTagDO bySlug = blogTagMapper.selectOne(
                new LambdaQueryWrapper<BlogTagDO>().eq(BlogTagDO::getSlug, slug).last("LIMIT 1"));
        if (bySlug != null) {
            return bySlug;
        }
        BlogTagDO created = new BlogTagDO();
        created.setName(name);
        created.setSlug(slug);
        blogTagMapper.insert(created);
        return created;
    }

    static String toSlug(String name) {
        String slug = name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-+|-+$", "");
        if (!StringUtils.hasText(slug)) {
            slug = "tag";
        }
        if (slug.length() > 64) {
            slug = slug.substring(0, 64);
        }
        return slug;
    }

    public List<Long> findPostIdsByTagNameOrSlug(String tag) {
        if (!StringUtils.hasText(tag)) {
            return List.of();
        }
        String value = tag.trim();
        BlogTagDO found = blogTagMapper.selectOne(new LambdaQueryWrapper<BlogTagDO>()
                .eq(BlogTagDO::getSlug, value)
                .or()
                .eq(BlogTagDO::getName, value)
                .last("LIMIT 1"));
        if (found == null) {
            return List.of();
        }
        return blogPostTagMapper.selectList(
                        new LambdaQueryWrapper<BlogPostTagDO>().eq(BlogPostTagDO::getTagId, found.getId()))
                .stream()
                .map(BlogPostTagDO::getPostId)
                .toList();
    }
}
