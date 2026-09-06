package com.byy.blogprojectbackend.archive.vo;

import java.util.List;

/**
 * 按年份分组的归档内容。
 */
public record ArchiveYearVO(
        int year,
        List<ArchiveMonthVO> months
) {
}
