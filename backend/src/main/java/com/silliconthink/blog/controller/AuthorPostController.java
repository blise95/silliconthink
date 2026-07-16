package com.silliconthink.blog.controller;

import com.silliconthink.auth.security.AuthUser;
import com.silliconthink.blog.dto.PageResult;
import com.silliconthink.blog.dto.PostCreateRequest;
import com.silliconthink.blog.dto.PostUpdateRequest;
import com.silliconthink.blog.dto.PostVO;
import com.silliconthink.blog.service.BlogPostService;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.common.Result;
import com.silliconthink.exception.BizException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/posts")
@RequiredArgsConstructor
public class AuthorPostController {

    private final BlogPostService blogPostService;

    @GetMapping
    public Result<PageResult<PostVO>> list(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = requireUser(authUser);
        return Result.ok(blogPostService.listMine(userId, page, pageSize, status));
    }

    @PostMapping
    public Result<PostVO> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PostCreateRequest request) {
        return Result.ok(blogPostService.create(requireUser(authUser), request));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return Result.ok(blogPostService.getMine(requireUser(authUser), id));
    }

    @PutMapping("/{id}")
    public Result<PostVO> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        return Result.ok(blogPostService.update(requireUser(authUser), id, request));
    }

    @PostMapping("/{id}/publish")
    public Result<PostVO> publish(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return Result.ok(blogPostService.publish(requireUser(authUser), id));
    }

    @PostMapping("/{id}/unpublish")
    public Result<PostVO> unpublish(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return Result.ok(blogPostService.unpublish(requireUser(authUser), id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        blogPostService.softDelete(requireUser(authUser), id);
        return Result.ok();
    }

    private static Long requireUser(AuthUser authUser) {
        if (authUser == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return authUser.getUserId();
    }
}
