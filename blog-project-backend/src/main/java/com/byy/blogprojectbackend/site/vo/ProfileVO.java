package com.byy.blogprojectbackend.site.vo;

import java.time.Instant;

public record ProfileVO(
        String id,
        String userId,
        String name,
        String avatar,
        String role,
        String bio,
        ProfileStatusVO status,
        ProfileStatsVO stats,
        Integer version,
        Instant updatedAt
) {

    public record ProfileStatusVO(
            String label,
            String text,
            String emoji
    ) {
    }

    public record ProfileStatsVO(
            int techPostCount,
            int momentCount,
            int guestbookCount
    ) {
    }
}