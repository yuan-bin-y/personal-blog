package com.byy.blogprojectbackend.media.storage;

import com.byy.blogprojectbackend.media.config.MediaStorageProperties;
import com.byy.blogprojectbackend.media.exception.MediaStorageException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(prefix = "app.media", name = "storage-provider", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageService implements ObjectStorageService {

    private final MediaStorageProperties properties;
    private final Path root;

    public LocalObjectStorageService(MediaStorageProperties properties) {
        this.properties = properties;
        this.root = Path.of(properties.getLocalRoot()).toAbsolutePath().normalize();
    }

    @Override
    public String provider() {
        return "local";
    }

    @Override
    public String bucketName() {
        return null;
    }

    @Override
    public void upload(String objectKey, InputStream input, long size, String contentType) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new MediaStorageException("本地媒体写入失败", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException exception) {
            throw new MediaStorageException("本地媒体删除失败", exception);
        }
    }

    @Override
    public String publicUrl(String objectKey) {
        return stripTrailingSlash(properties.getPublicBaseUrl()) + "/" + objectKey;
    }

    private Path resolve(String objectKey) {
        Path path = root.resolve(objectKey).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("非法媒体路径");
        }
        return path;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
