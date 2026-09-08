package com.byy.blogprojectbackend.search.vo;

/** AI 回答使用的站内内容来源。 */
public record AiSearchSourceVO(
        String postId,
        String slug,
        String title,
        String excerpt
) {
}
