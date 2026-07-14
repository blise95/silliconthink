package com.silliconthink.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.silliconthink.**.mapper")
public class MybatisPlusConfig {
}
