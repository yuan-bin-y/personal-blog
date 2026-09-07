package com.byy.blogprojectbackend.media.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/** 本地开发存储时，将 media-uploads 映射为只读 /media/**。 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.media", name = "storage-provider", havingValue = "local", matchIfMissing = true)
public class LocalMediaWebConfiguration implements WebMvcConfigurer {

    private final MediaStorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(properties.getLocalRoot())
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/media/**").addResourceLocations(location);
    }
}
