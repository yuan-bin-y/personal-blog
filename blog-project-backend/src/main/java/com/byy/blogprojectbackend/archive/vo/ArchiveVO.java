package com.byy.blogprojectbackend.archive.vo;

import java.util.List;

/**
 * 公开归档分页结果。
 */
public record ArchiveVO(
        List<ArchiveYearVO> groups,
        int page,
        int pageSize,
        long total,
        long totalPages,
        boolean hasMore
) {
}
