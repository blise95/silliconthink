package com.silliconthink.blog.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostVO {

    private String id;
    private String title;
    private String slug;
    private String summary;
    private String contentMd;
    private String coverUrl;
    private List<String> tags;
    private LocalDateTime publishedAt;
    private String status;
    private String authorDisplayName;
}
