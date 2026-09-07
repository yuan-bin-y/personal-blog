package com.byy.blogprojectbackend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录用户发表评论的请求。
 *
 * 作者、Post ID、创建时间等字段由服务端确定。
 */
public record CreateCommentDTO(

        @NotBlank(message = "评论内容不能为空")
        @Size(max = 2000, message = "评论内容不能超过 2000 个字符")
        String content

) {
}