package com.byy.blogprojectbackend.post.mapper.projection;

import lombok.Data;

/**
 * Post 与 Tag 的关联查询结果。
 */
@Data
public class PostTagRow {

    private Long postId;
    private Long tagId;
    private String tagName;
    private String tagSlug;
    private Boolean tagDeleted;
}