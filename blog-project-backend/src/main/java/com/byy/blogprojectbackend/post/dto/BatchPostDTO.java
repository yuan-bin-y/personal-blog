package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchPostDTO(
        @NotNull @Size(min = 1, max = 100) List<@Valid BatchPostItemDTO> items
) {
}
