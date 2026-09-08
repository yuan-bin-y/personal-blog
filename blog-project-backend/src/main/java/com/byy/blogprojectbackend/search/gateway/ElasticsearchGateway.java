package com.byy.blogprojectbackend.search.gateway;

import com.byy.blogprojectbackend.search.document.SearchDocument;

import java.util.List;

/** 隔离 Elasticsearch HTTP 协议与业务 Service。 */
public interface ElasticsearchGateway {
    SearchPage search(SearchQuery query);
    void createIndex(String indexName);
    void bulkIndex(String indexName, List<SearchDocument> documents);
    void switchAlias(String newIndexName);
    void deleteIndex(String indexName);
    void upsert(SearchDocument document);
    void delete(String postId);
}
