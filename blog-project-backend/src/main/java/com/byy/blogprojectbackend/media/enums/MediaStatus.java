package com.byy.blogprojectbackend.media.enums;

/** 媒体在数据库和对象存储之间的可恢复状态。 */
public enum MediaStatus {
    UPLOADING("上传中"),
    ACTIVE("可用"),
    UPLOAD_FAILED("上传失败"),
    DELETING("删除中"),
    DELETE_FAILED("删除失败"),
    DELETED("已删除");

    private final String label;

    MediaStatus(String label) {
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
