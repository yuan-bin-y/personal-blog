package com.byy.blogprojectbackend.common.exception;

/** 当前用户已经登录，但不是目标资源所有者或无权执行该业务操作。 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
