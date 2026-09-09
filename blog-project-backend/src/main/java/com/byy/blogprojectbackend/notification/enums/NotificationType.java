package com.byy.blogprojectbackend.notification.enums;

/** 当前通知中心支持的业务事件。 */
public enum NotificationType {
    COMMENT_CREATED("收到新评论"),
    GUESTBOOK_CREATED("收到新留言"),
    COMMENT_REPLIED("评论收到回复"),
    GUESTBOOK_REPLIED("留言收到回复");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
