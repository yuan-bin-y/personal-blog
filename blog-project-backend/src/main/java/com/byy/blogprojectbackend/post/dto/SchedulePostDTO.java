package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SchedulePostDTO(
        @NotNull @Future Instant publishAt,
        @NotNull @Min(0) Integer version
) {
}
