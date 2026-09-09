package com.byy.blogprojectbackend.common.enums;

import java.util.Locale;

/** Owner 列表通用状态筛选值。 */
public enum FilterStatus {
    ALL("全部"),
    ACTIVE("启用"),
    DISABLED("停用");

    public static final String DEFAULT_CODE = "ALL";
    public static final String VALIDATION_MESSAGE = "状态只能是全部（ALL）、启用（ACTIVE）或停用（DISABLED）";

    private final String label;

    FilterStatus(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }

    public static FilterStatus parse(String value) {
        String normalized = value == null ? DEFAULT_CODE : value.trim().toUpperCase(Locale.ROOT);
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(VALIDATION_MESSAGE);
        }
    }
}
