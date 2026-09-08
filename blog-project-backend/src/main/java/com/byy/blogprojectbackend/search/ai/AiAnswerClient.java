package com.byy.blogprojectbackend.search.ai;

import com.byy.blogprojectbackend.search.gateway.SearchHit;

import java.util.List;

/** 隔离具体大模型提供方的问答客户端。 */
public interface AiAnswerClient {
    String answer(String question, List<SearchHit> sources);
}
