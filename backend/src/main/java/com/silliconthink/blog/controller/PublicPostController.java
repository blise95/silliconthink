package com.silliconthink.blog.controller;

import com.silliconthink.blog.dto.PageResult;
import com.silliconthink.blog.dto.PostVO;
import com.silliconthink.blog.service.BlogPostService;
import com.silliconthink.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PublicPostController {

    private final BlogPostService blogPostService;

    @GetMapping
    public Result<PageResult<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        return Result.ok(blogPostService.listPublished(page, pageSize, tag, keyword));
    }

    @GetMapping("/by-slug/{slug}")
    public Result<PostVO> bySlug(@PathVariable String slug) {
        return Result.ok(blogPostService.getPublishedBySlug(slug));
    }

    @GetMapping("/latest")
    public Result<List<PostVO>> latest(@RequestParam(defaultValue = "3") int count) {
        return Result.ok(blogPostService.listLatestPublished(count));
    }
}
