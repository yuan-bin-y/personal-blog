package com.byy.blogprojectbackend.post.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.MomentDetailVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.post.vo.TechDetailVO;

public interface PostService {

    /**
     * 获取公开首页 Feed。
     */
    PageVO<PostSummaryVO> getPublicFeed(
            String type,
            int page,
            int pageSize
    );

    /**
     * 获取公开 TECH 列表。
     *
     * @param categorySlug 分类 slug，可为空
     * @param tagSlug      标签 slug，可为空
     */
    PageVO<PostSummaryVO> getPublicTechPosts(
            String categorySlug,
            String tagSlug,
            int page,
            int pageSize
    );

    /**
     * 根据 slug 获取公开 TECH 详情。
     */
    TechDetailVO getPublicTechPost(String slug);

    /**
     * 获取公开 MOMENT 列表。
     */
    PageVO<PostSummaryVO> getPublicMoments(
            Integer year,
            Integer month,
            int page,
            int pageSize
    );

    /**
     * 根据 ID 获取公开 MOMENT 详情。
     */
    MomentDetailVO getPublicMoment(Long id);
}
