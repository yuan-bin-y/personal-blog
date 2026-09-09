package com.byy.blogprojectbackend.post.enums;

/** post_media 中媒体与内容的关系。 */
public enum PostMediaUsage {
    COVER("封面"),
    CONTENT("正文媒体");

    private final String label;

    PostMediaUsage(String label) {
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
