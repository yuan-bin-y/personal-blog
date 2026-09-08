package com.byy.blogprojectbackend.search.event;

/** Post 事务提交后触发 Elasticsearch 增量同步。 */
public record PublishedPostChangedEvent(Long postId) {
}
