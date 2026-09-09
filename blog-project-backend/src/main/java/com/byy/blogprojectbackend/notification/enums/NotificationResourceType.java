package com.byy.blogprojectbackend.notification.enums;

/** 通知点击后所指向的业务资源。 */
public enum NotificationResourceType {
    POST("内容"),
    COMMENT("评论"),
    GUESTBOOK("留言");

    private final String label;

    NotificationResourceType(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
