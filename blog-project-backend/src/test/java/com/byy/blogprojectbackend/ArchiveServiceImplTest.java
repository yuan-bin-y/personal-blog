package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.archive.mapper.ArchiveMapper;
import com.byy.blogprojectbackend.archive.mapper.projection.ArchiveRow;
import com.byy.blogprojectbackend.archive.service.impl.ArchiveServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Visitor 公开归档业务测试。
 */
class ArchiveServiceImplTest {

    private final ArchiveMapper archiveMapper = mock(ArchiveMapper.class);
    private final ArchiveServiceImpl archiveService =
            new ArchiveServiceImpl(archiveMapper);

    @Test
    void getPublicArchive_whenEmpty_returnsEmptyPage() {
        when(archiveMapper.countPublicArchive(null, "ALL"))
                .thenReturn(0L);

        var archive = archiveService.getPublicArchive(
                null,
                "ALL",
                1,
                10
        );

        assertTrue(archive.groups().isEmpty());
        assertEquals(0, archive.totalPages());
        assertFalse(archive.hasMore());
    }

    @Test
    void getPublicArchive_groupsRowsByYearAndMonthInQueryOrder() {
        ArchiveRow septemberTech = row(
                1001L,
                "TECH",
                "redis-lua",
                "Redis + Lua",
                LocalDateTime.of(2026, 9, 6, 12, 0)
        );

        ArchiveRow augustMoment = row(
                1002L,
                "MOMENT",
                null,
                "今天整理了 BinSpace。",
                LocalDateTime.of(2026, 8, 20, 20, 0)
        );

        ArchiveRow oldTech = row(
                1003L,
                "TECH",
                "spring-security",
                "Spring Security",
                LocalDateTime.of(2025, 12, 1, 10, 0)
        );

        when(archiveMapper.countPublicArchive(null, "ALL"))
                .thenReturn(3L);
        when(archiveMapper.selectPublicArchive(null, "ALL", 0L, 10))
                .thenReturn(List.of(
                        septemberTech,
                        augustMoment,
                        oldTech
                ));

        var archive = archiveService.getPublicArchive(
                null,
                "ALL",
                1,
                10
        );

        assertEquals(2, archive.groups().size());
        assertEquals(2026, archive.groups().get(0).year());
        assertEquals(2, archive.groups().get(0).months().size());
        assertEquals(9, archive.groups().get(0).months().get(0).month());
        assertEquals("1001", archive.groups().get(0)
                .months().get(0).items().get(0).id());
        assertEquals(2025, archive.groups().get(1).year());
    }

    private ArchiveRow row(
            Long id,
            String type,
            String slug,
            String label,
            LocalDateTime publishedAt
    ) {
        ArchiveRow row = new ArchiveRow();
        row.setId(id);
        row.setType(type);
        row.setSlug(slug);
        row.setLabel(label);
        row.setPublishedAt(publishedAt);
        return row;
    }
}
