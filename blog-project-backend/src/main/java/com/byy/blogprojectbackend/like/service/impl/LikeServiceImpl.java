package com.byy.blogprojectbackend.like.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.like.entity.PostLike;
import com.byy.blogprojectbackend.like.mapper.LikeMapper;
import com.byy.blogprojectbackend.like.service.LikeService;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final IdGenerator idGenerator;

    @Override
    @Transactional
    public LikeStateVO like(Long postId, Long userId) {
        /*
         * 点赞只能针对公开 Post。
         * DRAFT、已删除、定时但尚未发布的 Post 对 Visitor 均视为不存在。
         */
        Integer currentLikeCount =
                likeMapper.selectPublicLikeCount(postId);

        if (currentLikeCount == null) {
            throw new ResourceNotFoundException("公开内容不存在");
        }

        PostLike postLike = new PostLike();
        postLike.setId(idGenerator.nextId());
        postLike.setPostId(postId);
        postLike.setUserId(userId);

        int inserted = likeMapper.insertIgnore(postLike);

        /*
         * 只有确实新增点赞关系时，才增加冗余点赞数。
         * 重复 PUT 时 inserted 为 0，不会重复增加。
         */
        if (inserted == 1) {
            int updated =
                    likeMapper.incrementPublicLikeCount(postId);

            /*
             * 防止检查完成后，Owner 恰好撤回或删除了文章。
             * 抛出异常后，事务会回滚前面插入的 post_like。
             */
            if (updated != 1) {
                throw new ResourceNotFoundException("公开内容不存在");
            }
        }

        Integer finalLikeCount =
                likeMapper.selectPublicLikeCount(postId);

        if (finalLikeCount == null) {
            throw new ResourceNotFoundException("公开内容不存在");
        }

        return new LikeStateVO(
                postId.toString(),
                true,
                finalLikeCount
        );
    }
}