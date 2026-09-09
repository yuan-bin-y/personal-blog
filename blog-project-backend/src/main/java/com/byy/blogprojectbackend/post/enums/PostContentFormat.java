package com.byy.blogprojectbackend.post.enums;

/** 正文存储格式。 */
public enum PostContentFormat {
    MARKDOWN("Markdown"),
    PLAIN_TEXT("纯文本");

    public static final String VALIDATION_PATTERN = "MARKDOWN|PLAIN_TEXT";
    public static final String VALIDATION_MESSAGE = "正文格式只能是 Markdown（MARKDOWN）或纯文本（PLAIN_TEXT）";

    private final String label;

    PostContentFormat(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
