package com.byy.blogprojectbackend.common.exception;

/** 资源名称、slug 或一对一回复等业务唯一性发生冲突。 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
