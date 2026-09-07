package com.byy.blogprojectbackend.media.storage;

import java.io.InputStream;

public interface ObjectStorageService {
    String provider();
    String bucketName();
    void upload(String objectKey, InputStream input, long size, String contentType);
    void delete(String objectKey);
    String publicUrl(String objectKey);
}
