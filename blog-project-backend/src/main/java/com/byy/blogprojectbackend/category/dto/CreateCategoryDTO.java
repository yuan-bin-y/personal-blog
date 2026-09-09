package com.byy.blogprojectbackend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Owner 创建分类请求。 */
public record CreateCategoryDTO(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 64, message = "分类名称不能超过 64 个字符") String name,
        @Size(max = 80, message = "分类 slug 不能超过 80 个字符") String slug,
        @Size(max = 255, message = "分类描述不能超过 255 个字符") String description,
        @NotNull(message = "分类排序值不能为空") Integer sortOrder
) {
}
