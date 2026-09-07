package com.byy.blogprojectbackend.media.vo;

import java.math.BigDecimal;
import java.time.Instant;

/** Owner 媒体库对外返回的数据，不暴露 bucket 和 objectKey。 */
public record MediaAssetVO(
        String id,
        String mediaType,
        String usageType,
        String url,
        String originalName,
        String contentType,
        long size,
        Integer width,
        Integer height,
        BigDecimal durationSeconds,
        Instant createdAt
) {
}
