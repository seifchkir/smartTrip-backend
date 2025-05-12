package com.SmarTrip.smarTrip_backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:4000", "http://127.0.0.1:4200", "http://127.0.0.1:4000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false) // Change to false when using multiple origins with wildcards
                .maxAge(3600);
    }
}