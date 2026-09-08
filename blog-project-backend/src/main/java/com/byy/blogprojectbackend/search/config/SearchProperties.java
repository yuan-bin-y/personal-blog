package com.byy.blogprojectbackend.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Elasticsearch 连接、索引别名和重建批次配置。 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.search")
public class SearchProperties {
    private String baseUrl = "http://localhost:9200";
    private String username;
    private String password;
    private String indexAlias = "binspace-posts";
    private int reindexBatchSize = 200;
}
