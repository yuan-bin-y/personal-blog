package com.byy.blogprojectbackend.common.vo;

import java.util.List;

/**
 * 统一分页响应。
 */
public record PageVO<T>(
        List<T> items,
        int page,
        int pageSize,
        long total,
        long totalPages,
        boolean hasMore
) {
}