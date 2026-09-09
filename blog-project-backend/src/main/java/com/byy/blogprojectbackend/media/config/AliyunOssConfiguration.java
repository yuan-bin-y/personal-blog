package com.byy.blogprojectbackend.media.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.byy.blogprojectbackend.media.enums.MediaStorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "app.media",
        name = "storage-provider",
        havingValue = MediaStorageProvider.ALIYUN_OSS_CODE
)
public class AliyunOssConfiguration {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(MediaStorageProperties properties) {
        MediaStorageProperties.Oss oss = properties.getOss();
        requireText(oss.getEndpoint(), "OSS_ENDPOINT");
        requireText(oss.getAccessKeyId(), "OSS_ACCESS_KEY_ID");
        requireText(oss.getAccessKeySecret(), "OSS_ACCESS_KEY_SECRET");
        requireText(oss.getBucketName(), "OSS_BUCKET_NAME");
        return new OSSClientBuilder().build(
                oss.getEndpoint(),
                oss.getAccessKeyId(),
                oss.getAccessKeySecret()
        );
    }

    private void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 未配置");
        }
    }
}
