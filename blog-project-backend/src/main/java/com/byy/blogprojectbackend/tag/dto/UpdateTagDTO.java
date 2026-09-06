package com.byy.blogprojectbackend.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTagDTO(
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(max = 80) String slug
) {
}
