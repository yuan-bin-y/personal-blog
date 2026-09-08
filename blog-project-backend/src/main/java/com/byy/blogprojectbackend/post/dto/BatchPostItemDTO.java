package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BatchPostItemDTO(
        @NotBlank @Pattern(regexp = "[1-9]\\d*") String postId,
        @NotNull @Min(0) Integer version
) {
}
