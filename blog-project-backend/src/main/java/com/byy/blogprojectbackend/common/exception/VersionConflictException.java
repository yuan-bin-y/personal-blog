package com.byy.blogprojectbackend.common.exception;

/**
 * 前端提交的乐观锁版本已经过期。
 */
public class VersionConflictException extends RuntimeException {

    public VersionConflictException(String message) {
        super(message);
    }
}
