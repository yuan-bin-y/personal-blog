package com.byy.blogprojectbackend.archive.vo;

import java.util.List;

/**
 * 按月份分组的归档内容。
 */
public record ArchiveMonthVO(
        int month,
        List<ArchiveItemVO> items
) {
}
