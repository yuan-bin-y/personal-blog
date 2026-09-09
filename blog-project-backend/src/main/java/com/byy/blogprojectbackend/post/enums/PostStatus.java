package com.byy.blogprojectbackend.post.enums;

/** 内容发布状态。code 用于数据库/API，label 用于中文展示。 */
public enum PostStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    SCHEDULED("定时发布");

    /** 普通新建/编辑接口只允许草稿或立即发布；定时发布由独立接口处理。 */
    public static final String EDITABLE_PATTERN = "DRAFT|PUBLISHED";
    public static final String EDITABLE_MESSAGE = "发布状态只能是草稿（DRAFT）或已发布（PUBLISHED）";

    private final String label;

    PostStatus(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }

    public boolean matches(String value) {
        return name().equals(value);
    }
}
