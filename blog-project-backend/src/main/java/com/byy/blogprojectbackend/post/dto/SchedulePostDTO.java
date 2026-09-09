package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SchedulePostDTO(
        @NotNull(message = "发布时间不能为空")
        @Future(message = "发布时间必须晚于当前时间") Instant publishAt,
        @NotNull(message = "版本号不能为空")
        @Min(value = 0, message = "版本号不能小于 0") Integer version
) {
}
