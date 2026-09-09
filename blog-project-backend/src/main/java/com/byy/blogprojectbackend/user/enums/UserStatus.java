package com.byy.blogprojectbackend.user.enums;

/** 用户账号状态。 */
public enum UserStatus {
    ACTIVE("正常"),
    DISABLED("已停用"),
    LOCKED("已锁定");

    private final String label;

    UserStatus(String label) {
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
