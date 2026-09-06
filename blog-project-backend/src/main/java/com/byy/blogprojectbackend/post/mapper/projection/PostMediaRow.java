package com.byy.blogprojectbackend.post.mapper.projection;

import lombok.Data;

/**
 * Post 媒体查询结果。
 */
@Data
public class PostMediaRow {

    private Long postId;
    private Long id;
    private String usageType;
    private String mediaType;
    private String url;
    private String posterUrl;
    private String altText;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
}