package com.byy.blogprojectbackend.media.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** media_asset 表实体；保存对象存储定位信息及操作状态。 */
@Data
public class MediaAsset {
    private Long id;
    private String mediaType;
    private String usageType;
    private String storageProvider;
    private String bucketName;
    private String objectKey;
    private String url;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private BigDecimal durationSeconds;
    private String status;
    private Integer retryCount;
    private String lastError;
    private LocalDateTime nextRetryAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;
}
