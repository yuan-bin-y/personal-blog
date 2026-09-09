package com.byy.blogprojectbackend.post.enums;

/** Owner 内容列表的发布状态筛选值。 */
public enum PostStatusFilter {
    ALL("全部状态"),
    DRAFT("草稿"),
    SCHEDULED("定时发布"),
    PUBLISHED("已发布");

    public static final String DEFAULT_CODE = "ALL";
    public static final String VALIDATION_PATTERN = "ALL|DRAFT|SCHEDULED|PUBLISHED";
    public static final String VALIDATION_MESSAGE =
            "发布状态只能是全部（ALL）、草稿（DRAFT）、定时发布（SCHEDULED）或已发布（PUBLISHED）";

    private final String label;

    PostStatusFilter(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
