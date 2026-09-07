package com.byy.blogprojectbackend.media.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 媒体大小限制、本地存储与 OSS 私密配置。 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.media")
public class MediaStorageProperties {
    private String storageProvider = "local";
    private String localRoot = "media-uploads";
    private String publicBaseUrl = "http://localhost:8080/media";
    private long maxImageBytes = 10 * 1024 * 1024L;
    private long maxVideoBytes = 200 * 1024 * 1024L;
    private long maxAudioBytes = 30 * 1024 * 1024L;
    private boolean retryEnabled = true;
    private long retryDelayMs = 60_000L;
    private Oss oss = new Oss();

    @Getter
    @Setter
    public static class Oss {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
    }
}
