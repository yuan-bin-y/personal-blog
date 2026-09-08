package com.byy.blogprojectbackend.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 基于已发布 BinSpace 内容进行问答的请求。 */
public record AiSearchDTO(
        @NotBlank(message = "question 不能为空")
        @Size(max = 500, message = "question 不能超过500个字符")
        String question
) {
}
