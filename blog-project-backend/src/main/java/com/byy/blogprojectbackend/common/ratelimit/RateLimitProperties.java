package com.byy.blogprojectbackend.common.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Redis 固定窗口限流参数；按业务风险分别配置，不把所有接口套同一阈值。 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private int loginPerMinute = 10;
    private int registerPerHour = 5;
    private int interactionPerMinute = 12;
    private int likePerMinute = 60;
    private int mediaUploadPerMinute = 20;
    private int searchPerMinute = 60;
}
