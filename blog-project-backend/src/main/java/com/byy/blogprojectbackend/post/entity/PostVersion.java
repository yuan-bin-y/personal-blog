package com.byy.blogprojectbackend.post.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** Post 正式变更前产生的不可变历史快照。 */
@Data
@TableName("post_version")
public class PostVersion {
    @TableId
    private Long id;
    private Long postId;
    private Integer versionNo;
    private String postType;
    private String summary;
    private String snapshotJson;
    private Long createdBy;
    private LocalDateTime createdAt;
}
