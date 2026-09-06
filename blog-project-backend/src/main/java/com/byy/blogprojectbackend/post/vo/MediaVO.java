package com.byy.blogprojectbackend.post.vo;

public record MediaVO(
        String id,
        String usageType,
        String mediaType,
        String src,
        String poster,
        String alt,
        Integer width,
        Integer height,
        Integer sortOrder
) {
}