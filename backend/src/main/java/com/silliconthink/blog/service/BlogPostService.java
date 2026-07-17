package com.silliconthink.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silliconthink.blog.dto.PageResult;
import com.silliconthink.blog.dto.PostCreateRequest;
import com.silliconthink.blog.dto.PostUpdateRequest;
import com.silliconthink.blog.dto.PostVO;
import com.silliconthink.blog.entity.BlogPostDO;
import com.silliconthink.blog.mapper.BlogPostMapper;
import com.silliconthink.blog.storage.BlogObjectKeys;
import com.silliconthink.blog.storage.BlogObjectStore;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.exception.BizException;
import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogPostService {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final BlogPostMapper blogPostMapper;
    private final BlogTagService blogTagService;
    private final UserService userService;
    private final BlogObjectStore blogObjectStore;

    public PageResult<PostVO> listPublished(int page, int pageSize, String tag, String keyword) {
        LambdaQueryWrapper<BlogPostDO> qw = publishedQuery();
        applyTagFilter(qw, tag);
        applyKeywordFilter(qw, keyword);
        qw.orderByDesc(BlogPostDO::getPublishedAt);
        return toPage(qw, page, pageSize, true, false);
    }

    public PostVO getPublishedBySlug(String slug) {
        BlogPostDO post = blogPostMapper.selectOne(publishedQuery()
                .eq(BlogPostDO::getSlug, slug)
                .last("LIMIT 1"));
        if (post == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return toVo(post, blogTagService.listTagNamesByPostId(post.getId()), true, true);
    }

    public List<PostVO> listLatestPublished(int count) {
        int size = Math.min(Math.max(count, 1), 20);
        List<BlogPostDO> posts = blogPostMapper.selectList(publishedQuery()
                .orderByDesc(BlogPostDO::getPublishedAt)
                .last("LIMIT " + size));
        return toVoList(posts, true, false);
    }

    public PageResult<PostVO> listMine(Long authorId, int page, int pageSize, String status) {
        LambdaQueryWrapper<BlogPostDO> qw = new LambdaQueryWrapper<BlogPostDO>()
                .eq(BlogPostDO::getAuthorId, authorId);
        if (StringUtils.hasText(status)) {
            qw.eq(BlogPostDO::getStatus, status.trim());
        }
        qw.orderByDesc(BlogPostDO::getUpdateDate);
        return toPage(qw, page, pageSize, true, false);
    }

    public PostVO getMine(Long authorId, Long postId) {
        BlogPostDO post = requireOwned(authorId, postId);
        return toVo(post, blogTagService.listTagNamesByPostId(post.getId()), true, true);
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

        String key = BlogObjectKeys.postContent(authorId, post.getId());
        try {
            if (!blogObjectStore.isRootWritable()) {
                throw new BizException(ErrorCode.MEDIA_STORAGE_UNAVAILABLE);
            }
            String body = nullToEmpty(request.getContentMd());
            blogObjectStore.putString(key, body);
            post.setContentKey(key);
            // Keep content_md as fallback until object store is fully trusted in prod
            post.setContentMd(body);
            blogPostMapper.updateById(post);
        } catch (RuntimeException e) {
            blogPostMapper.deleteById(post.getId());
            throw e;
        }

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
        post.setCoverUrl(blankToNull(request.getCoverUrl()));

        if (!blogObjectStore.isRootWritable()) {
            throw new BizException(ErrorCode.MEDIA_STORAGE_UNAVAILABLE);
        }
        String key = StringUtils.hasText(post.getContentKey())
                ? post.getContentKey()
                : BlogObjectKeys.postContent(authorId, postId);
        String body = nullToEmpty(request.getContentMd());
        blogObjectStore.putString(key, body);
        post.setContentKey(key);
        post.setContentMd(body);
        blogPostMapper.updateById(post);
        blogTagService.replacePostTags(postId, request.getTags());
        return getMine(authorId, postId);
    }

    @Transactional
    public PostVO publish(Long authorId, Long postId) {
        BlogPostDO post = requireOwned(authorId, postId);
        String body = loadContent(post);
        if (!StringUtils.hasText(post.getTitle())
                || !StringUtils.hasText(post.getSlug())
                || !StringUtils.hasText(body)) {
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
        // Soft-delete keeps object for recovery; slug released for reuse
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

    private PageResult<PostVO> toPage(
            LambdaQueryWrapper<BlogPostDO> qw, int page, int pageSize, boolean withAuthor, boolean withContent) {
        int p = Math.max(page, 1);
        int size = Math.min(Math.max(pageSize, 1), 50);
        Page<BlogPostDO> result = blogPostMapper.selectPage(new Page<>(p, size), qw);
        return PageResult.<PostVO>builder()
                .list(toVoList(result.getRecords(), withAuthor, withContent))
                .total(result.getTotal())
                .page(p)
                .pageSize(size)
                .build();
    }

    private List<PostVO> toVoList(List<BlogPostDO> posts, boolean withAuthor, boolean withContent) {
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
                .map(post -> toVo(post, tags.getOrDefault(post.getId(), List.of()), finalNames.get(post.getAuthorId()), withContent))
                .toList();
    }

    private PostVO toVo(BlogPostDO post, List<String> tags, boolean withAuthor, boolean withContent) {
        String displayName = null;
        if (withAuthor) {
            UserDO user = userService.findById(post.getAuthorId());
            if (user != null) {
                displayName = user.getDisplayName();
            }
        }
        return toVo(post, tags, displayName, withContent);
    }

    private PostVO toVo(BlogPostDO post, List<String> tags, String authorDisplayName, boolean withContent) {
        String contentMd = null;
        if (withContent) {
            contentMd = loadContent(post);
        }
        return PostVO.builder()
                .id(String.valueOf(post.getId()))
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .contentMd(contentMd)
                .coverUrl(post.getCoverUrl())
                .tags(tags)
                .publishedAt(post.getPublishedAt())
                .status(post.getStatus())
                .authorDisplayName(authorDisplayName)
                .build();
    }

    /**
     * Prefer object store; fall back to legacy content_md when the object is missing
     * (e.g. storage root moved) so published posts do not hard-404.
     */
    String loadContent(BlogPostDO post) {
        if (StringUtils.hasText(post.getContentKey())) {
            var fromStore = blogObjectStore.getString(post.getContentKey());
            if (fromStore.isPresent()) {
                return fromStore.get();
            }
            if (StringUtils.hasText(post.getContentMd())) {
                log.warn("Content object missing for key={}, falling back to content_md (postId={})",
                        post.getContentKey(), post.getId());
                return post.getContentMd();
            }
            throw new BizException(ErrorCode.CONTENT_OBJECT_MISSING);
        }
        if (StringUtils.hasText(post.getContentMd())) {
            return post.getContentMd();
        }
        return "";
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
        blogPostMapper.releaseDeletedSlug(normalized, excludeId);
    }

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
