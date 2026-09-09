package com.byy.blogprojectbackend.like.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.like.entity.PostLike;
import com.byy.blogprojectbackend.like.mapper.LikeMapper;
import com.byy.blogprojectbackend.like.service.LikeService;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostMediaRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostTagRow;
import com.byy.blogprojectbackend.post.enums.PostMediaUsage;
import com.byy.blogprojectbackend.post.enums.PostStatus;
import com.byy.blogprojectbackend.post.enums.PostType;
import com.byy.blogprojectbackend.post.vo.CategoryVO;
import com.byy.blogprojectbackend.post.vo.MediaVO;
import com.byy.blogprojectbackend.post.vo.PostAuthorVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.post.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
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

    /**
     * 幂等取消点赞。只有真正删除关系时才扣减冗余计数，
     * 重复 DELETE 会直接返回当前未点赞状态。
     */
    @Override
    @Transactional
    public LikeStateVO unlike(Long postId, Long userId) {
        requirePublicLikeCount(postId);

        int deleted = likeMapper.deleteRelation(postId, userId);
        if (deleted == 1
                && likeMapper.decrementPublicLikeCount(postId) != 1) {
            // 回滚刚删除的关系，防止文章状态并发变化造成计数不一致。
            throw new ResourceNotFoundException("公开内容不存在");
        }

        int finalLikeCount = requirePublicLikeCount(postId);
        return new LikeStateVO(postId.toString(), false, finalLikeCount);
    }

    /**
     * 先按点赞关系分页 Post 主体，再批量补齐标签和媒体，
     * 避免多表 JOIN 产生重复 Post 并破坏分页。
     */
    @Override
    public PageVO<PostSummaryVO> listMyLikes(
            Long userId,
            int page,
            int pageSize
    ) {
        long total = likeMapper.countMyPublicLikes(userId);
        long totalPages = (total + pageSize - 1) / pageSize;

        if (total == 0) {
            return new PageVO<>(List.of(), page, pageSize, 0, 0, false);
        }

        long offset = (long) (page - 1) * pageSize;
        List<PostFeedRow> postRows = likeMapper.selectMyPublicLikes(
                userId,
                offset,
                pageSize
        );

        if (postRows.isEmpty()) {
            return new PageVO<>(
                    List.of(),
                    page,
                    pageSize,
                    total,
                    totalPages,
                    false
            );
        }

        List<Long> postIds = postRows.stream()
                .map(PostFeedRow::getId)
                .toList();
        Map<Long, List<TagVO>> tagsByPostId = groupTags(
                postMapper.selectTagsByPostIds(postIds)
        );
        Map<Long, List<MediaVO>> mediaByPostId = groupMedia(
                postMapper.selectMediaByPostIds(postIds)
        );

        List<PostSummaryVO> items = postRows.stream()
                .map(row -> toPostSummary(
                        row,
                        tagsByPostId.getOrDefault(row.getId(), List.of()),
                        mediaByPostId.getOrDefault(row.getId(), List.of())
                ))
                .toList();

        return new PageVO<>(
                items,
                page,
                pageSize,
                total,
                totalPages,
                page < totalPages
        );
    }

    private int requirePublicLikeCount(Long postId) {
        Integer likeCount = likeMapper.selectPublicLikeCount(postId);
        if (likeCount == null) {
            throw new ResourceNotFoundException("公开内容不存在");
        }
        return likeCount;
    }

    private PostSummaryVO toPostSummary(
            PostFeedRow row,
            List<TagVO> tags,
            List<MediaVO> media
    ) {
        boolean tech = PostType.TECH.matches(row.getType());
        boolean moment = PostType.MOMENT.matches(row.getType());
        PostAuthorVO author = new PostAuthorVO(
                String.valueOf(row.getAuthorUserId()),
                row.getAuthorName(),
                row.getAuthorAvatar()
        );

        CategoryVO category = null;
        if (tech && row.getCategoryId() != null) {
            category = new CategoryVO(
                    String.valueOf(row.getCategoryId()),
                    row.getCategoryName(),
                    row.getCategorySlug(),
                    row.getCategoryDescription(),
                    !Boolean.TRUE.equals(row.getCategoryDeleted())
            );
        }

        MediaVO cover = tech
                ? media.stream()
                        .filter(item -> PostMediaUsage.COVER.matches(item.usageType()))
                        .findFirst()
                        .orElse(null)
                : null;
        List<MediaVO> images = moment
                ? media.stream()
                        .filter(item -> PostMediaUsage.CONTENT.matches(item.usageType()))
                        .toList()
                : List.of();

        return new PostSummaryVO(
                String.valueOf(row.getId()),
                row.getType(),
                tech ? row.getSlug() : null,
                tech ? row.getTitle() : null,
                tech ? row.getSummary() : null,
                moment ? row.getContent() : null,
                author,
                category,
                tech ? tags : List.of(),
                cover,
                images,
                toInstant(row.getCreatedAt()),
                toInstant(row.getUpdatedAt()),
                toInstant(row.getPublishedAt()),
                tech ? row.getReadingTime() : null,
                PostStatus.PUBLISHED.code(),
                row.getLikeCount(),
                true,
                row.getCommentCount(),
                null
        );
    }

    private Map<Long, List<TagVO>> groupTags(List<PostTagRow> rows) {
        Map<Long, List<TagVO>> result = new HashMap<>();
        for (PostTagRow row : rows) {
            TagVO tag = new TagVO(
                    String.valueOf(row.getTagId()),
                    row.getTagName(),
                    row.getTagSlug(),
                    !Boolean.TRUE.equals(row.getTagDeleted())
            );
            result.computeIfAbsent(row.getPostId(), key -> new ArrayList<>()).add(tag);
        }
        return result;
    }

    private Map<Long, List<MediaVO>> groupMedia(List<PostMediaRow> rows) {
        Map<Long, List<MediaVO>> result = new HashMap<>();
        for (PostMediaRow row : rows) {
            MediaVO media = new MediaVO(
                    String.valueOf(row.getId()),
                    row.getUsageType(),
                    row.getMediaType(),
                    row.getUrl(),
                    row.getPosterUrl(),
                    row.getAltText(),
                    row.getWidth(),
                    row.getHeight(),
                    row.getSortOrder()
            );
            result.computeIfAbsent(row.getPostId(), key -> new ArrayList<>()).add(media);
        }
        return result;
    }

    private java.time.Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
