package com.byy.blogprojectbackend.search.vo;

import java.util.List;

/** 携带来源的 AI 内容回答。 */
public record AiSearchVO(
        String answer,
        List<AiSearchSourceVO> sources
) {
}
