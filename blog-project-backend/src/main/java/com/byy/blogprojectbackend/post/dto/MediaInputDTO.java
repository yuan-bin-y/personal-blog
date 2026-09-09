package com.byy.blogprojectbackend.post.dto;

import com.byy.blogprojectbackend.media.enums.MediaType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Post 封面或内容媒体输入；当前只保存已存在的资源 URL。 */
public record MediaInputDTO(
        @NotBlank(message = "媒体地址不能为空")
        @Size(max = 1024, message = "媒体地址不能超过 1024 个字符") String src,

        @NotNull(message = "媒体类型不能为空")
        @Pattern(regexp = MediaType.POST_MEDIA_PATTERN, message = MediaType.POST_MEDIA_MESSAGE)
        String mediaType,

        @Size(max = 1024, message = "视频封面地址不能超过 1024 个字符") String poster,
        @Size(max = 255, message = "媒体替代文本不能超过 255 个字符") String alt,
        @Min(value = 1, message = "媒体宽度必须大于 0") Integer width,
        @Min(value = 1, message = "媒体高度必须大于 0") Integer height,
        @Min(value = 0, message = "媒体排序值不能小于 0") Integer sortOrder
) {
}
