package com.byy.blogprojectbackend.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** OpenAI-compatible 内容问答提供方配置。 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    private String baseUrl;
    private String chatCompletionsPath = "/v1/chat/completions";
    private String apiKey;
    private String model;
    private int contextPostLimit = 5;
    private int maxContextCharacters = 12_000;
    private int requestsPerMinute = 10;

    public boolean isConfigured() {
        return hasText(baseUrl) && hasText(chatCompletionsPath)
                && chatCompletionsPath.startsWith("/")
                && hasText(apiKey) && hasText(model);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
