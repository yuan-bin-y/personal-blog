package com.byy.blogprojectbackend.post.enums;

/** 内容列表的类型筛选值。 */
public enum PostTypeFilter {
    ALL("全部内容"),
    TECH("技术文章"),
    MOMENT("说说");

    public static final String DEFAULT_CODE = "ALL";
    public static final String VALIDATION_PATTERN = "ALL|TECH|MOMENT";
    public static final String VALIDATION_MESSAGE = "内容类型只能是全部（ALL）、技术文章（TECH）或说说（MOMENT）";

    private final String label;

    PostTypeFilter(String label) {
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
