package com.byy.blogprojectbackend.post.vo;

import java.time.Instant;

public record AutosaveVO(String postId, Instant savedAt, Integer baseVersion) {
}
