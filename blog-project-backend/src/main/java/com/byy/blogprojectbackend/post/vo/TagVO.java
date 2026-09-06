package com.byy.blogprojectbackend.post.vo;

public record TagVO(
        String id,
        String name,
        String slug,
        boolean active
) {
}