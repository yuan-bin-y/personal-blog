package com.byy.blogprojectbackend.media.enums;

public enum MediaType {
    IMAGE("图片"),
    VIDEO("视频"),
    AUDIO("音频");

    /** post_media 当前允许的媒体类型；音频只用于空间音乐。 */
    public static final String POST_MEDIA_PATTERN = "IMAGE|VIDEO";
    public static final String POST_MEDIA_MESSAGE = "文章媒体类型只能是图片（IMAGE）或视频（VIDEO）";

    private final String label;

    MediaType(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
