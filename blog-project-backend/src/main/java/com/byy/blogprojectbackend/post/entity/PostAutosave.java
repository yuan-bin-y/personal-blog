package com.byy.blogprojectbackend.post.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 编辑器自动保存记录；它不会覆盖正式 Post。 */
@Data
@TableName("post_autosave")
public class PostAutosave {
    @TableId
    private Long id;
    private Long postId;
    private String postType;
    private String payloadJson;
    private Integer baseVersion;
    private LocalDateTime savedAt;
    private Long updatedBy;
}
