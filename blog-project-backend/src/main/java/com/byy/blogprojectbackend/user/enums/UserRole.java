package com.byy.blogprojectbackend.user.enums;

/** BinSpace V1 用户角色。 */
public enum UserRole {
    OWNER("空间主人"),
    VISITOR("访客");

    private final String label;

    UserRole(String label) {
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
