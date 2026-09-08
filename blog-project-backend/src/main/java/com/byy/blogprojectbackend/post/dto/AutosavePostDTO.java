package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record AutosavePostDTO(
        @NotNull @Pattern(regexp = "TECH|MOMENT") String type,
        @NotNull Map<String, Object> payload,
        @NotNull @Min(0) Integer baseVersion
) {
}
