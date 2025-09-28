package com.example.citymanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Все пути перенаправляем на index.html для SPA
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/cities").setViewName("forward:/index.html");
        registry.addViewController("/cities/**").setViewName("forward:/index.html");
    }
}