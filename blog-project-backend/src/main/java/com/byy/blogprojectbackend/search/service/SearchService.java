package com.byy.blogprojectbackend.search.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.search.dto.AiSearchDTO;
import com.byy.blogprojectbackend.search.vo.AiSearchVO;
import com.byy.blogprojectbackend.search.vo.SearchResultVO;

import java.util.List;

/** 公开搜索、相关内容和 AI 问答业务。 */
public interface SearchService {
    PageVO<SearchResultVO> search(
            String keyword,
            String type,
            String categorySlug,
            String tagSlug,
            int page,
            int pageSize
    );

    List<PostSummaryVO> related(Long postId, int limit);

    AiSearchVO ask(AiSearchDTO dto, String clientKey);
}
