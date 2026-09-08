package com.byy.blogprojectbackend.post.mapper.projection;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVersionRow {
    private Long id;
    private Long postId;
    private Integer versionNo;
    private String postType;
    private String summary;
    private String snapshotJson;
    private LocalDateTime createdAt;
}
