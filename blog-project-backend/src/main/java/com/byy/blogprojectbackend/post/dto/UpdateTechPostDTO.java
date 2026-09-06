package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record UpdateTechPostDTO(
        @NotBlank @Size(max=180) String title,
        @NotBlank @Size(max=600) String summary,
        @NotBlank String content,
        @NotNull @Pattern(regexp="MARKDOWN|PLAIN_TEXT") String contentFormat,
        @NotBlank @Pattern(regexp="[1-9]\\d*") String categoryId,
        @NotNull @Size(max=10) List<@Pattern(regexp="[1-9]\\d*") String> tagIds,
        @Valid MediaInputDTO cover,
        @NotNull @Pattern(regexp="DRAFT|PUBLISHED") String status,
        @NotNull @Min(0) Integer version
) {
}
