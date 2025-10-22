package com.study.aihub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC通用配置类
 * @author wangrui
 * @date 2025/10/20
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    /**
     * 添加跨域请求
     * @param registry
     * @author wangrui
     * @date 2025/10/20
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }
}
