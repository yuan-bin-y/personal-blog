package com.byy.blogprojectbackend.media.enums;

public enum MediaUsageType {
    AVATAR("头像"),
    HERO("首页 Hero"),
    POST_COVER("文章封面"),
    POST_CONTENT("内容媒体"),
    MUSIC("空间音乐"),
    PAGE_BACKGROUND("页面背景");

    private final String label;

    MediaUsageType(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
