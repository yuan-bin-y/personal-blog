package com.byy.blogprojectbackend.media.exception;

public class MediaPayloadTooLargeException extends RuntimeException {
    public MediaPayloadTooLargeException(String message) {
        super(message);
    }
}
