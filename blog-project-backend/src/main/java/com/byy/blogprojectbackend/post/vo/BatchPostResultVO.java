package com.byy.blogprojectbackend.post.vo;

import java.util.List;

public record BatchPostResultVO(
        List<String> succeededIds,
        List<BatchPostFailureVO> failed
) {
}
