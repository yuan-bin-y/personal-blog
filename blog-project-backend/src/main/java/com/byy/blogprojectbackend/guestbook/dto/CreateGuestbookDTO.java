package com.byy.blogprojectbackend.guestbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 登录用户发布空间留言的请求；作者信息由服务端从当前身份生成。 */
public record CreateGuestbookDTO(

        @NotBlank(message = "留言内容不能为空")
        @Size(max = 2000, message = "留言内容不能超过 2000 个字符")
        String content

) {
}
