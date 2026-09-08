package com.byy.blogprojectbackend.realtime.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.realtime.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class RealtimeController {

    private final RealtimeService realtimeService;

    @GetMapping(
            value = "/api/realtime/events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<SseEmitter> subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        SseEmitter emitter = realtimeService.subscribe(
                requiredUserId(jwt),
                lastEventId
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }
}
