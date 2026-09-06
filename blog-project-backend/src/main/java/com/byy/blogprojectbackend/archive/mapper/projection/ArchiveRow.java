package com.byy.blogprojectbackend.archive.mapper.projection;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 归档 SQL 的精简查询结果。
 */
@Data
public class ArchiveRow {

    private Long id;
    private String type;
    private String slug;
    private String label;
    private LocalDateTime publishedAt;
}
