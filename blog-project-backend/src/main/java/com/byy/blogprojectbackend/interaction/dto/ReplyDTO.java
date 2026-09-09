package com.byy.blogprojectbackend.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Owner 回复评论或留言时共用的请求。 */
public record ReplyDTO(
        @NotBlank(message = "回复内容不能为空")
        @Size(max = 2000, message = "回复内容不能超过 2000 个字符") String content
) {
}
