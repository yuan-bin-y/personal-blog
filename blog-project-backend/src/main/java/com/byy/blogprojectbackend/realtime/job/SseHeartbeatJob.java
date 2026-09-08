package com.byy.blogprojectbackend.realtime.job;

import com.byy.blogprojectbackend.realtime.registry.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 发送 SSE 注释帧，防止代理或浏览器因长时间无数据关闭连接。 */
@Component
@RequiredArgsConstructor
public class SseHeartbeatJob {

    private final SseConnectionRegistry registry;

    @Scheduled(fixedDelayString = "${app.realtime.heartbeat-ms:25000}")
    public void heartbeat() {
        registry.heartbeat();
    }
}
