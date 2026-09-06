package com.byy.blogprojectbackend.post.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** post_media 表写入实体。 */
@Data
public class PostMedia {
    private Long id;
    private Long postId;
    private String usageType;
    private String mediaType;
    private String url;
    private String posterUrl;
    private String altText;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
