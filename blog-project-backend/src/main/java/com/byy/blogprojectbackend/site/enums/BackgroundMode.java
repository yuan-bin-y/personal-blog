package com.byy.blogprojectbackend.site.enums;

/** 空间背景展示模式。 */
public enum BackgroundMode {
    VIDEO("video", "视频背景"),
    WALLPAPER("wallpaper", "静态壁纸");

    public static final String VALIDATION_PATTERN = "video|wallpaper";
    public static final String VALIDATION_MESSAGE = "背景模式只能是视频背景（video）或静态壁纸（wallpaper）";

    private final String code;
    private final String label;

    BackgroundMode(String code, String label) {
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
