package com.byy.blogprojectbackend.search.gateway.impl;

import com.byy.blogprojectbackend.search.config.SearchProperties;
import com.byy.blogprojectbackend.search.document.SearchDocument;
import com.byy.blogprojectbackend.search.exception.SearchUpstreamException;
import com.byy.blogprojectbackend.search.gateway.ElasticsearchGateway;
import com.byy.blogprojectbackend.search.gateway.SearchHit;
import com.byy.blogprojectbackend.search.gateway.SearchPage;
import com.byy.blogprojectbackend.search.gateway.SearchQuery;
import com.byy.blogprojectbackend.post.enums.PostTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 使用 Elasticsearch REST API 完成查询、批量写入和别名切换。 */
@Component
public class RestElasticsearchGateway implements ElasticsearchGateway {
    private static final Logger log = LoggerFactory.getLogger(RestElasticsearchGateway.class);
    private static final String INDEX_NAME_PATTERN = "[a-z0-9._-]+";

    private final SearchProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient client;

    public RestElasticsearchGateway(
            SearchProperties properties,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        requireSafeIndexName(properties.getIndexAlias());
        RestClient.Builder builder = restClientBuilder.clone()
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()));
        if (hasText(properties.getUsername())) {
            builder.defaultHeaders(headers -> headers.setBasicAuth(
                    properties.getUsername(),
                    properties.getPassword() == null ? "" : properties.getPassword()
            ));
        }
        this.client = builder.build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchPage search(SearchQuery query) {
        List<Object> filters = new ArrayList<>();
        if (!PostTypeFilter.ALL.matches(query.type())) {
            filters.add(Map.of("term", Map.of("type", query.type())));
        }
        if (hasText(query.categorySlug())) {
            filters.add(Map.of("term", Map.of("categorySlug", query.categorySlug())));
        }
        if (hasText(query.tagSlug())) {
            filters.add(Map.of("term", Map.of("tagSlugs", query.tagSlug())));
        }

        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", query.keyword());
        multiMatch.put("fields", List.of("title^4", "summary^2", "content", "categoryName^2", "tagNames^2"));
        multiMatch.put("type", "best_fields");

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("must", List.of(Map.of("multi_match", multiMatch)));
        bool.put("filter", filters);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", (query.page() - 1) * query.pageSize());
        body.put("size", query.pageSize());
        body.put("track_total_hits", true);
        body.put("query", Map.of("bool", bool));
        body.put("sort", List.of("_score", Map.of("publishedAt", Map.of("order", "desc"))));
        body.put("highlight", Map.of(
                "pre_tags", List.of("<em>"),
                "post_tags", List.of("</em>"),
                "fragment_size", 160,
                "number_of_fragments", 2,
                "fields", Map.of("title", Map.of(), "summary", Map.of(), "content", Map.of())
        ));

        try {
            Map<String, Object> response = request(
                    HttpMethod.POST,
                    "/" + properties.getIndexAlias() + "/_search",
                    body,
                    Map.class
            );
            Map<String, Object> hitsBlock = asMap(response.get("hits"));
            long total = ((Number) asMap(hitsBlock.get("total")).getOrDefault("value", 0)).longValue();
            List<SearchHit> results = new ArrayList<>();

            for (Object item : asList(hitsBlock.get("hits"))) {
                Map<String, Object> hit = asMap(item);
                Map<String, Object> source = asMap(hit.get("_source"));
                Map<String, Object> highlight = asMap(hit.get("highlight"));
                String summary = text(source.get("summary"));
                String content = text(source.get("content"));
                String excerpt = firstText(summary, content);
                results.add(new SearchHit(
                        text(source.get("id")),
                        text(source.get("type")),
                        nullableText(source.get("slug")),
                        nullableText(source.get("title")),
                        truncate(excerpt, 220),
                        truncate(firstText(content, summary), 4_000),
                        flattenHighlights(highlight),
                        Instant.parse(text(source.get("publishedAt")))
                ));
            }
            return new SearchPage(List.copyOf(results), total);
        } catch (SearchUpstreamException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new SearchUpstreamException("Elasticsearch 搜索结果无法解析", exception);
        }
    }

    @Override
    public void createIndex(String indexName) {
        requireSafeIndexName(indexName);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", Map.of("type", "keyword"));
        fields.put("type", Map.of("type", "keyword"));
        fields.put("slug", Map.of("type", "keyword"));
        fields.put("title", Map.of("type", "text", "analyzer", "standard"));
        fields.put("summary", Map.of("type", "text", "analyzer", "standard"));
        fields.put("content", Map.of("type", "text", "analyzer", "standard"));
        fields.put("categorySlug", Map.of("type", "keyword"));
        fields.put("categoryName", Map.of("type", "text", "analyzer", "standard"));
        fields.put("tagSlugs", Map.of("type", "keyword"));
        fields.put("tagNames", Map.of("type", "text", "analyzer", "standard"));
        fields.put("publishedAt", Map.of("type", "date"));

        request(
                HttpMethod.PUT,
                "/" + indexName,
                Map.of(
                        "settings", Map.of("number_of_shards", 1, "number_of_replicas", 0),
                        "mappings", Map.of("dynamic", "strict", "properties", fields)
                ),
                Map.class
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bulkIndex(String indexName, List<SearchDocument> documents) {
        if (documents.isEmpty()) {
            return;
        }
        requireSafeIndexName(indexName);
        StringBuilder ndjson = new StringBuilder();
        try {
            for (SearchDocument document : documents) {
                ndjson.append(objectMapper.writeValueAsString(
                        Map.of("index", Map.of("_index", indexName, "_id", document.id()))
                )).append('\n');
                ndjson.append(objectMapper.writeValueAsString(document)).append('\n');
            }

            Map<String, Object> response = client.post()
                    .uri("/_bulk")
                    .contentType(MediaType.parseMediaType("application/x-ndjson"))
                    .body(ndjson.toString())
                    .retrieve()
                    .body(Map.class);
            if (response == null || Boolean.TRUE.equals(response.get("errors"))) {
                throw new SearchUpstreamException("Elasticsearch 批量写入部分失败");
            }
        } catch (SearchUpstreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new SearchUpstreamException("Elasticsearch 批量写入失败", exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void switchAlias(String newIndexName) {
        requireSafeIndexName(newIndexName);
        List<String> oldIndexes = findAliasIndexes();
        List<Object> actions = new ArrayList<>();
        for (String oldIndex : oldIndexes) {
            if (!oldIndex.equals(newIndexName)) {
                actions.add(Map.of("remove", Map.of("index", oldIndex, "alias", properties.getIndexAlias())));
            }
        }
        actions.add(Map.of("add", Map.of("index", newIndexName, "alias", properties.getIndexAlias())));
        request(HttpMethod.POST, "/_aliases", Map.of("actions", actions), Map.class);

        // 别名已原子切换，旧索引删除失败不影响新索引对外服务。
        for (String oldIndex : oldIndexes) {
            if (!oldIndex.equals(newIndexName)) {
                try {
                    request(HttpMethod.DELETE, "/" + oldIndex, null, Map.class);
                } catch (RuntimeException exception) {
                    log.warn("旧搜索索引清理失败：{}", oldIndex);
                }
            }
        }
    }

    @Override
    public void upsert(SearchDocument document) {
        try {
            request(
                    HttpMethod.PUT,
                    "/" + properties.getIndexAlias() + "/_doc/" + document.id() + "?require_alias=true",
                    document,
                    Map.class
            );
        } catch (RestClientResponseException exception) {
            throw new SearchUpstreamException("Elasticsearch 增量索引写入失败", exception);
        }
    }

    @Override
    public void deleteIndex(String indexName) {
        requireSafeIndexName(indexName);
        try {
            request(HttpMethod.DELETE, "/" + indexName, null, Map.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() != 404) {
                throw exception;
            }
        }
    }

    @Override
    public void delete(String postId) {
        try {
            request(
                    HttpMethod.DELETE,
                    "/" + properties.getIndexAlias() + "/_doc/" + postId,
                    null,
                    Map.class
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() != 404) {
                throw new SearchUpstreamException("Elasticsearch 增量索引删除失败", exception);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> findAliasIndexes() {
        try {
            List<Map<String, Object>> rows = client.get()
                    .uri("/_cat/aliases/" + properties.getIndexAlias() + "?format=json&h=index")
                    .retrieve()
                    .body(List.class);
            if (rows == null) {
                return List.of();
            }
            return rows.stream()
                    .map(row -> text(row.get("index")))
                    .filter(this::hasText)
                    .distinct()
                    .toList();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return List.of();
            }
            throw new SearchUpstreamException("Elasticsearch 别名查询失败", exception);
        }
    }

    private <T> T request(HttpMethod method, String path, Object body, Class<T> responseType) {
        try {
            RestClient.RequestBodySpec spec = client.method(method).uri(path);
            if (body != null) {
                spec.contentType(MediaType.APPLICATION_JSON).body(body);
            }
            return spec.retrieve().body(responseType);
        } catch (RestClientResponseException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new SearchUpstreamException("Elasticsearch 请求失败", exception);
        }
    }

    private List<String> flattenHighlights(Map<String, Object> highlight) {
        List<String> result = new ArrayList<>();
        for (Object value : highlight.values()) {
            for (Object fragment : asList(value)) {
                if (fragment != null) {
                    result.add(String.valueOf(fragment));
                }
            }
        }
        return List.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private List<?> asList(Object value) {
        return value instanceof List<?> list ? list : List.of();
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : (second == null ? "" : second);
    }

    private String nullableText(Object value) {
        String result = text(value);
        return result.isBlank() ? null : result;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String truncate(String value, int max) {
        String normalized = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max) + "…";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void requireSafeIndexName(String value) {
        if (!hasText(value) || !value.matches(INDEX_NAME_PATTERN)) {
            throw new IllegalArgumentException("Elasticsearch 索引别名或名称不合法");
        }
    }

    private String trimTrailingSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
