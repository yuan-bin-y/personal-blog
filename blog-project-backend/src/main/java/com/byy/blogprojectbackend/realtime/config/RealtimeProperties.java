package com.byy.blogprojectbackend.realtime.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** SSE 超时、心跳和断线补发上限。 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.realtime")
public class RealtimeProperties {
    private long timeoutMs = 30 * 60 * 1000L;
    private long heartbeatMs = 25_000L;
    private int replayLimit = 100;
}
