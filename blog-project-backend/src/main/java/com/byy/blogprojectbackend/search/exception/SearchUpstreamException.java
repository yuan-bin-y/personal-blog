package com.byy.blogprojectbackend.search.exception;

/** Elasticsearch 不可用或返回异常结构。 */
public class SearchUpstreamException extends RuntimeException {
    public SearchUpstreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchUpstreamException(String message) {
        super(message);
    }
}
