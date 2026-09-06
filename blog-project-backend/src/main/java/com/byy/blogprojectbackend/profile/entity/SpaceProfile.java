package com.byy.blogprojectbackend.profile.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * {@code profile} 表实体，保存空间主人的公开展示资料。
 *
 * <p>登录账号与公开资料分表，避免认证字段和展示字段混在一起。</p>
 */
@Data
@TableName("profile")
public class SpaceProfile {

    @TableId
    private Long id;

    private Long userId;

    private String displayName;

    private String avatarUrl;

    private String roleText;

    private String bio;

    private String statusLabel;

    private String statusText;

    private String statusEmoji;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
