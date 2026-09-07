package com.byy.blogprojectbackend.media.enums;

/** 媒体在数据库和对象存储之间的可恢复状态。 */
public enum MediaStatus {
    UPLOADING,
    ACTIVE,
    UPLOAD_FAILED,
    DELETING,
    DELETE_FAILED,
    DELETED
}
