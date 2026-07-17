package com.silliconthink.config;

import com.silliconthink.blog.storage.BlogObjectStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class BlogStorageHealthWarner implements ApplicationRunner {

    private final BlogObjectStore blogObjectStore;

    @Override
    public void run(ApplicationArguments args) {
        if (!blogObjectStore.isRootWritable()) {
            log.warn("Blog object storage root is not writable: {}. Uploads and content writes will fail until fixed.",
                    blogObjectStore.rootPath());
        } else {
            log.info("Blog object storage root OK: {}", blogObjectStore.rootPath());
        }
    }
}
