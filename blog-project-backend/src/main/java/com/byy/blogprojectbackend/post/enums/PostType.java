package com.byy.blogprojectbackend.post.enums;

/** Post 的业务类型。code 用于数据库/API，label 用于中文展示。 */
public enum PostType {
    TECH("技术文章"),
    MOMENT("说说");

    public static final String VALIDATION_PATTERN = "TECH|MOMENT";
    public static final String VALIDATION_MESSAGE = "内容类型只能是技术文章（TECH）或说说（MOMENT）";

    private final String label;

    PostType(String label) {
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
