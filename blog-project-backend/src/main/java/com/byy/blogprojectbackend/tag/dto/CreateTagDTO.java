package com.byy.blogprojectbackend.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagDTO(
        @NotBlank @Size(max = 64) String name,
        @Size(max = 80) String slug
) {
}
