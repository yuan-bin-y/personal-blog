package com.byy.blogprojectbackend.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Owner 更新分类请求。 */
public record UpdateCategoryDTO(
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(max = 80) String slug,
        @Size(max = 255) String description,
        @NotNull Integer sortOrder,
        @NotNull @Min(0) Integer version
) {
}
