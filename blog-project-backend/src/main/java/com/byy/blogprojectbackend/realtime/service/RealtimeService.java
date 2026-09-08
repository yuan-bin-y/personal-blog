package com.byy.blogprojectbackend.realtime.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RealtimeService {
    SseEmitter subscribe(Long userId, String lastEventId);
}
