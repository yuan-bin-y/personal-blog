package com.byy.blogprojectbackend.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Owner 回复评论或留言时共用的请求。 */
public record ReplyDTO(@NotBlank @Size(max=2000) String content) {
}
