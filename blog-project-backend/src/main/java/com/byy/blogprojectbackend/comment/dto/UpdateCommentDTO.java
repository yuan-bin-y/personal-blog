package com.byy.blogprojectbackend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 修改自己发布的顶层评论；作者等服务端字段不可修改。 */
public record UpdateCommentDTO(

        @NotBlank(message = "评论内容不能为空")
        @Size(max = 2000, message = "评论内容不能超过 2000 个字符")
        String content

) {
}
