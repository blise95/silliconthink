package com.silliconthink.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostUpdateRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 200)
    private String slug;

    @Size(max = 500)
    private String summary = "";

    /** 草稿允许空正文；发布时由服务层再校验 */
    private String contentMd = "";

    @Size(max = 512)
    private String coverUrl;

    private List<String> tags = new ArrayList<>();
}
