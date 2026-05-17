package com.lqx.opera.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final AdminOperationLogInterceptor adminOperationLogInterceptor;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor,
                        AdminOperationLogInterceptor adminOperationLogInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.adminOperationLogInterceptor = adminOperationLogInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");

        registry.addInterceptor(adminOperationLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Map /files/** to local uploads directory
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:uploads/");
    }
}

