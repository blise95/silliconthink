package com.silliconthink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SiliconThinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiliconThinkApplication.class, args);
    }
}
