package com.byy.blogprojectbackend.archive.service.impl;

import com.byy.blogprojectbackend.archive.mapper.ArchiveMapper;
import com.byy.blogprojectbackend.archive.mapper.projection.ArchiveRow;
import com.byy.blogprojectbackend.archive.service.ArchiveService;
import com.byy.blogprojectbackend.archive.vo.ArchiveItemVO;
import com.byy.blogprojectbackend.archive.vo.ArchiveMonthVO;
import com.byy.blogprojectbackend.archive.vo.ArchiveVO;
import com.byy.blogprojectbackend.archive.vo.ArchiveYearVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor 公开归档业务实现。
 */
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveMapper archiveMapper;

    @Override
    public ArchiveVO getPublicArchive(
            Integer year,
            String type,
            int page,
            int pageSize
    ) {
        long total = archiveMapper.countPublicArchive(
                year,
                type
        );

        if (total == 0) {
            return new ArchiveVO(
                    List.of(),
                    page,
                    pageSize,
                    0,
                    0,
                    false
            );
        }

        long offset = (long) (page - 1) * pageSize;

        List<ArchiveRow> rows =
                archiveMapper.selectPublicArchive(
                        year,
                        type,
                        offset,
                        pageSize
                );

        long totalPages =
                (total + pageSize - 1) / pageSize;

        return new ArchiveVO(
                groupByYearAndMonth(rows),
                page,
                pageSize,
                total,
                totalPages,
                page < totalPages
        );
    }

    /**
     * SQL 已按发布时间倒序，LinkedHashMap 保留该顺序完成年/月分组。
     */
    private List<ArchiveYearVO> groupByYearAndMonth(
            List<ArchiveRow> rows
    ) {
        Map<Integer, Map<Integer, List<ArchiveItemVO>>> grouped =
                new LinkedHashMap<>();

        for (ArchiveRow row : rows) {
            LocalDateTime publishedAt = row.getPublishedAt();

            int year = publishedAt.getYear();
            int month = publishedAt.getMonthValue();

            ArchiveItemVO item = new ArchiveItemVO(
                    String.valueOf(row.getId()),
                    row.getType(),
                    row.getSlug(),
                    row.getLabel(),
                    publishedAt.toInstant(ZoneOffset.UTC)
            );

            grouped
                    .computeIfAbsent(
                            year,
                            key -> new LinkedHashMap<>()
                    )
                    .computeIfAbsent(
                            month,
                            key -> new ArrayList<>()
                    )
                    .add(item);
        }

        List<ArchiveYearVO> result = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<ArchiveItemVO>>> yearEntry
                : grouped.entrySet()) {
            List<ArchiveMonthVO> months = new ArrayList<>();

            for (Map.Entry<Integer, List<ArchiveItemVO>> monthEntry
                    : yearEntry.getValue().entrySet()) {
                months.add(new ArchiveMonthVO(
                        monthEntry.getKey(),
                        monthEntry.getValue()
                ));
            }

            result.add(new ArchiveYearVO(
                    yearEntry.getKey(),
                    months
            ));
        }

        return result;
    }
}
