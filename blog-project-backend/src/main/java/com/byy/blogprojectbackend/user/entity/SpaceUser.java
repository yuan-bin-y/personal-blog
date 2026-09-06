package com.byy.blogprojectbackend.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * {@code space_user} 表实体，只保存登录与账号状态信息。
 *
 * <p>该对象仅用于持久化，不直接作为 API 响应返回。</p>
 */
@Data
@TableName("space_user")
public class SpaceUser {

    @TableId
    private Long id;

    private String username;

    private String passwordHash;

    private String role;

    private String status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime deletedAt;
}
