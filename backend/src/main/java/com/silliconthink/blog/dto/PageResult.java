package com.silliconthink.blog.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
}
