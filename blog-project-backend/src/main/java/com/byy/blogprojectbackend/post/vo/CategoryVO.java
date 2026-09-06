package com.byy.blogprojectbackend.post.vo;

public record CategoryVO(
        String id,
        String name,
        String slug,
        String description,
        boolean active
) {
}