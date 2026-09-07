package com.byy.blogprojectbackend.media.validation;

import com.byy.blogprojectbackend.media.enums.MediaType;

import java.math.BigDecimal;

public record DetectedMedia(
        MediaType mediaType,
        String contentType,
        String extension,
        Integer width,
        Integer height,
        BigDecimal durationSeconds
) {
}
