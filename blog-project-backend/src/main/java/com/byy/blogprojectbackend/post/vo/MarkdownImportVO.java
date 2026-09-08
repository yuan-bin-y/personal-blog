package com.byy.blogprojectbackend.post.vo;

import java.util.Map;

public record MarkdownImportVO(
        String title,
        String content,
        Map<String, Object> frontMatter
) {
}
