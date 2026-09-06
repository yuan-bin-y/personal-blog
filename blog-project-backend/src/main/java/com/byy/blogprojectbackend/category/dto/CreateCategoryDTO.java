package com.byy.blogprojectbackend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Owner 创建分类请求。 */
public record CreateCategoryDTO(
        @NotBlank @Size(max = 64) String name,
        @Size(max = 80) String slug,
        @Size(max = 255) String description,
        @NotNull Integer sortOrder
) {
}
