package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Post 封面或内容媒体输入；当前只保存已存在的资源 URL。 */
public record MediaInputDTO(
        @NotBlank @Size(max = 1024) String src,
        @NotNull @Pattern(regexp = "IMAGE|VIDEO") String mediaType,
        @Size(max = 1024) String poster,
        @Size(max = 255) String alt,
        @Min(1) Integer width,
        @Min(1) Integer height,
        @Min(0) Integer sortOrder
) {
}
