package com.byy.blogprojectbackend.category.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** category 表实体。 */
@Data
@TableName("category")
public class Category {
    @TableId
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
