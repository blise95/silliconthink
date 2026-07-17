package com.silliconthink.blog.storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class BlogObjectKeys {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private BlogObjectKeys() {
    }

    public static String postContent(Long authorId, Long postId) {
        return "posts/" + authorId + "/" + postId + ".md";
    }

    public static String media(String filename) {
        String datePath = LocalDate.now().format(DATE_PATH);
        return "media/" + datePath + "/" + filename;
    }
}
