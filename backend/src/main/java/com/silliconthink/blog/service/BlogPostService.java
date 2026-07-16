package com.silliconthink.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silliconthink.blog.dto.PageResult;
import com.silliconthink.blog.dto.PostCreateRequest;
import com.silliconthink.blog.dto.PostUpdateRequest;
import com.silliconthink.blog.dto.PostVO;
import com.silliconthink.blog.entity.BlogPostDO;
import com.silliconthink.blog.mapper.BlogPostMapper;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.exception.BizException;
import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final BlogPostMapper blogPostMapper;
    private final BlogTagService blogTagService;
    private final UserService userService;

    public PageResult<PostVO> listPublished(int page, int pageSize, String tag, String keyword) {
        LambdaQueryWrapper<BlogPostDO> qw = publishedQuery();
        applyTagFilter(qw, tag);
        applyKeywordFilter(qw, keyword);
        qw.orderByDesc(BlogPostDO::getPublishedAt);
        return toPage(qw, page, pageSize, true);
    }

    public PostVO getPublishedBySlug(String slug) {
        BlogPostDO post = blogPostMapper.selectOne(publishedQuery()
                .eq(BlogPostDO::getSlug, slug)
                .last("LIMIT 1"));
        if (post == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return toVo(post, blogTagService.listTagNamesByPostId(post.getId()), true);
    }

    public List<PostVO> listLatestPublished(int count) {
        int size = Math.min(Math.max(count, 1), 20);
        List<BlogPostDO> posts = blogPostMapper.selectList(publishedQuery()
                .orderByDesc(BlogPostDO::getPublishedAt)
                .last("LIMIT " + size));
        return toVoList(posts, true);
    }

    public PageResult<PostVO> listMine(Long authorId, int page, int pageSize, String status) {
        LambdaQueryWrapper<BlogPostDO> qw = new LambdaQueryWrapper<BlogPostDO>()
                .eq(BlogPostDO::getAuthorId, authorId);
        if (StringUtils.hasText(status)) {
            qw.eq(BlogPostDO::getStatus, status.trim());
        }
        qw.orderByDesc(BlogPostDO::getUpdateDate);
        return toPage(qw, page, pageSize, true);
    }

    public PostVO getMine(Long authorId, Long postId) {
        BlogPostDO post = requireOwned(authorId, postId);
        return toVo(post, blogTagService.listTagNamesByPostId(post.getId()), true);
    }

    @Transactional
    public PostVO create(Long authorId, PostCreateRequest request) {
        validateSlug(request.getSlug());
        assertSlugAvailable(request.getSlug(), null);
        BlogPostDO post = new BlogPostDO();
        post.setAuthorId(authorId);
        post.setTitle(request.getTitle().trim());
        post.setSlug(normalizeSlug(request.getSlug()));
        post.setSummary(nullToEmpty(request.getSummary()));
        post.setContentMd(nullToEmpty(request.getContentMd()));
        post.setCoverUrl(blankToNull(request.getCoverUrl()));
        post.setStatus(STATUS_DRAFT);
        post.setPublishedAt(null);
        blogPostMapper.insert(post);
        blogTagService.replacePostTags(post.getId(), request.getTags());
        return getMine(authorId, post.getId());
    }

    @Transactional
    public PostVO update(Long authorId, Long postId, PostUpdateRequest request) {
        BlogPostDO post = requireOwned(authorId, postId);
        validateSlug(request.getSlug());
        String slug = normalizeSlug(request.getSlug());
        assertSlugAvailable(slug, postId);
        post.setTitle(request.getTitle().trim());
        post.setSlug(slug);
        post.setSummary(nullToEmpty(request.getSummary()));
        post.setContentMd(nullToEmpty(request.getContentMd()));
        post.setCoverUrl(blankToNull(request.getCoverUrl()));
        blogPostMapper.updateById(post);
        blogTagService.replacePostTags(postId, request.getTags());
        return getMine(authorId, postId);
    }

    @Transactional
    public PostVO publish(Long authorId, Long postId) {
        BlogPostDO post = requireOwned(authorId, postId);
        if (!StringUtils.hasText(post.getTitle())
                || !StringUtils.hasText(post.getSlug())
                || !StringUtils.hasText(post.getContentMd())) {
            throw new BizException(ErrorCode.PUBLISH_INCOMPLETE);
        }
        post.setStatus(STATUS_PUBLISHED);
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        blogPostMapper.updateById(post);
        return getMine(authorId, postId);
    }

    @Transactional
    public PostVO unpublish(Long authorId, Long postId) {
        BlogPostDO post = requireOwned(authorId, postId);
        post.setStatus(STATUS_DRAFT);
        blogPostMapper.updateById(post);
        return getMine(authorId, postId);
    }

    @Transactional
    public void softDelete(Long authorId, Long postId) {
        BlogPostDO post = requireOwned(authorId, postId);
        // 软删除前释放 slug，否则 uk_blog_post_slug 仍占用，无法复用
        post.setSlug(releaseSlug(post.getSlug(), post.getId()));
        post.setStatus(STATUS_DRAFT);
        blogPostMapper.updateById(post);
        blogPostMapper.deleteById(postId);
    }

    private BlogPostDO requireOwned(Long authorId, Long postId) {
        BlogPostDO post = blogPostMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!Objects.equals(post.getAuthorId(), authorId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return post;
    }

    private LambdaQueryWrapper<BlogPostDO> publishedQuery() {
        return new LambdaQueryWrapper<BlogPostDO>()
                .eq(BlogPostDO::getStatus, STATUS_PUBLISHED);
    }

    private void applyTagFilter(LambdaQueryWrapper<BlogPostDO> qw, String tag) {
        if (!StringUtils.hasText(tag)) {
            return;
        }
        List<Long> postIds = blogTagService.findPostIdsByTagNameOrSlug(tag);
        if (postIds.isEmpty()) {
            qw.eq(BlogPostDO::getId, -1L);
        } else {
            qw.in(BlogPostDO::getId, postIds);
        }
    }

    private void applyKeywordFilter(LambdaQueryWrapper<BlogPostDO> qw, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        qw.and(w -> w.like(BlogPostDO::getTitle, kw).or().like(BlogPostDO::getSummary, kw));
    }

    private PageResult<PostVO> toPage(LambdaQueryWrapper<BlogPostDO> qw, int page, int pageSize, boolean withAuthor) {
        int p = Math.max(page, 1);
        int size = Math.min(Math.max(pageSize, 1), 50);
        Page<BlogPostDO> result = blogPostMapper.selectPage(new Page<>(p, size), qw);
        return PageResult.<PostVO>builder()
                .list(toVoList(result.getRecords(), withAuthor))
                .total(result.getTotal())
                .page(p)
                .pageSize(size)
                .build();
    }

    private List<PostVO> toVoList(List<BlogPostDO> posts, boolean withAuthor) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> ids = posts.stream().map(BlogPostDO::getId).toList();
        Map<Long, List<String>> tags = blogTagService.listTagNamesByPostIds(ids);
        Map<Long, String> displayNames = Map.of();
        if (withAuthor) {
            List<Long> authorIds = posts.stream().map(BlogPostDO::getAuthorId).distinct().toList();
            displayNames = authorIds.stream()
                    .map(userService::findById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(UserDO::getId, UserDO::getDisplayName, (a, b) -> a));
        }
        Map<Long, String> finalNames = displayNames;
        return posts.stream()
                .map(post -> toVo(post, tags.getOrDefault(post.getId(), List.of()), finalNames.get(post.getAuthorId())))
                .toList();
    }

    private PostVO toVo(BlogPostDO post, List<String> tags, boolean withAuthor) {
        String displayName = null;
        if (withAuthor) {
            UserDO user = userService.findById(post.getAuthorId());
            if (user != null) {
                displayName = user.getDisplayName();
            }
        }
        return toVo(post, tags, displayName);
    }

    private PostVO toVo(BlogPostDO post, List<String> tags, String authorDisplayName) {
        return PostVO.builder()
                .id(String.valueOf(post.getId()))
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .contentMd(post.getContentMd())
                .coverUrl(post.getCoverUrl())
                .tags(tags)
                .publishedAt(post.getPublishedAt())
                .status(post.getStatus())
                .authorDisplayName(authorDisplayName)
                .build();
    }

    private void validateSlug(String slug) {
        String normalized = normalizeSlug(slug);
        if (!SLUG_PATTERN.matcher(normalized).matches()) {
            throw new BizException(ErrorCode.INVALID_SLUG);
        }
    }

    private void assertSlugAvailable(String slug, Long excludeId) {
        String normalized = normalizeSlug(slug);
        LambdaQueryWrapper<BlogPostDO> active = new LambdaQueryWrapper<BlogPostDO>()
                .eq(BlogPostDO::getSlug, normalized);
        if (excludeId != null) {
            active.ne(BlogPostDO::getId, excludeId);
        }
        if (blogPostMapper.selectCount(active) > 0) {
            throw new BizException(ErrorCode.SLUG_EXISTS);
        }
        // 活跃行未占用时，清理软删行上残留的同名 slug（历史数据 / 并发兜底）
        blogPostMapper.releaseDeletedSlug(normalized, excludeId);
    }

    /** 软删除后改写 slug，腾出唯一索引给新文章复用 */
    private static String releaseSlug(String slug, Long id) {
        String suffix = "__del_" + id;
        String base = StringUtils.hasText(slug) ? slug : "post";
        int maxBase = 200 - suffix.length();
        if (base.length() > maxBase) {
            base = base.substring(0, Math.max(maxBase, 0));
        }
        return base + suffix;
    }

    private static String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
