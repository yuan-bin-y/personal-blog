package com.byy.blogprojectbackend.media.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.byy.blogprojectbackend.media.config.MediaStorageProperties;
import com.byy.blogprojectbackend.media.exception.MediaStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.media", name = "storage-provider", havingValue = "aliyun-oss")
public class AliyunOssObjectStorageService implements ObjectStorageService {

    private final OSS ossClient;
    private final MediaStorageProperties properties;

    @Override
    public String provider() {
        return "aliyun-oss";
    }

    @Override
    public String bucketName() {
        return properties.getOss().getBucketName();
    }

    @Override
    public void upload(String objectKey, InputStream input, long size, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size);
            metadata.setContentType(contentType);
            ossClient.putObject(bucketName(), objectKey, input, metadata);
        } catch (RuntimeException exception) {
            throw new MediaStorageException("OSS 媒体上传失败", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            // OSS DeleteObject 对不存在的 key 也可重复执行，适合失败重试。
            ossClient.deleteObject(bucketName(), objectKey);
        } catch (RuntimeException exception) {
            throw new MediaStorageException("OSS 媒体删除失败", exception);
        }
    }

    @Override
    public String publicUrl(String objectKey) {
        return stripTrailingSlash(properties.getPublicBaseUrl()) + "/" + objectKey;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
