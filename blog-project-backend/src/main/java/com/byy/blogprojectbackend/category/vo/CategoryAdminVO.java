package com.byy.blogprojectbackend.category.vo;

import java.time.Instant;

/** Owner 分类管理视图。 */
public record CategoryAdminVO(
        String id,
        String name,
        String slug,
        String description,
        boolean active,
        int sortOrder,
        int version,
        Instant updatedAt
) {
}
