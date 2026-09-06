package com.byy.blogprojectbackend.tag.vo;

import java.time.Instant;

public record TagAdminVO(String id, String name, String slug, boolean active, Instant updatedAt) {
}
