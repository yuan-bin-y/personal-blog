package com.byy.blogprojectbackend.guestbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 修改自己发布的顶层留言；作者等服务端字段不可修改。 */
public record UpdateGuestbookDTO(

        @NotBlank(message = "留言内容不能为空")
        @Size(max = 2000, message = "留言内容不能超过 2000 个字符")
        String content

) {
}
