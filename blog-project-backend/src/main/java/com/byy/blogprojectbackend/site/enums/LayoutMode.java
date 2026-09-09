package com.byy.blogprojectbackend.site.enums;

/** 空间首页布局模式。 */
public enum LayoutMode {
    STANDARD("standard", "标准首页"),
    IMMERSIVE("immersive", "沉浸首页");

    public static final String VALIDATION_PATTERN = "standard|immersive";
    public static final String VALIDATION_MESSAGE = "布局模式只能是标准首页（standard）或沉浸首页（immersive）";

    private final String code;
    private final String label;

    LayoutMode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }
}
