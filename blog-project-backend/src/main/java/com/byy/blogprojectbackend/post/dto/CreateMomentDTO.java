package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateMomentDTO(
        @NotBlank @Size(max=2000) String content,
        @NotNull @Size(max=9) List<@Valid MediaInputDTO> images,
        @NotNull @Pattern(regexp="DRAFT|PUBLISHED") String status
) {
}
