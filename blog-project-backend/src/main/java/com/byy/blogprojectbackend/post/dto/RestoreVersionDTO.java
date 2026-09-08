package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RestoreVersionDTO(@NotNull @Min(0) Integer version) {
}
