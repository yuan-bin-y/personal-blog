package com.byy.blogprojectbackend.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Search 模块共用的同步 HTTP 客户端构建器。 */
@Configuration
public class SearchHttpClientConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
