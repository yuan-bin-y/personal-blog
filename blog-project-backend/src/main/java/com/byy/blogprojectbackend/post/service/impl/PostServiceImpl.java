package com.byy.blogprojectbackend.post.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostMediaRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostTagRow;
import com.byy.blogprojectbackend.post.service.PostService;
import com.byy.blogprojectbackend.post.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor Post 查询业务实现。
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final String TECH = "TECH";
    private static final String MOMENT = "MOMENT";
    private static final String COVER = "COVER";
    private static final String CONTENT = "CONTENT";

    private final PostMapper postMapper;

    @Override
    public PageVO<PostSummaryVO> getPublicFeed(
            String type,
            int page,
            int pageSize
    ) {
        return getPublicPosts(
                type,
                null,
                null,
                page,
                pageSize
        );
    }

    /**
     * 获取公开 TECH 列表。
     *
     * <p>与首页 Feed 共用分页和组装逻辑，只把类型固定为 TECH，
     * 并增加 Category/Tag slug 筛选。</p>
     */
    @Override
    public PageVO<PostSummaryVO> getPublicTechPosts(
            String categorySlug,
            String tagSlug,
            int page,
            int pageSize
    ) {
        return getPublicPosts(
                TECH,
                normalizeSlug(categorySlug),
                normalizeSlug(tagSlug),
                page,
                pageSize
        );
    }

    /**
     * 根据 slug 获取公开 TECH 详情。
     */
    @Override
    public TechDetailVO getPublicTechPost(String slug) {
        PostFeedRow techRow =
                postMapper.selectPublicTechBySlug(slug);

        if (techRow == null) {
            throw new ResourceNotFoundException(
                    "技术文章不存在"
            );
        }

        /*
         * 第一版相关文章使用“同分类的最新 TECH”。
         * 主文章本身通过 postId 排除。
         */
        List<PostFeedRow> relatedRows =
                postMapper.selectRelatedPublicTech(
                        techRow.getId(),
                        techRow.getCategoryId(),
                        3
                );

        /*
         * 主文章与相关文章的关联数据一次批量查询，
         * 避免分别为每篇文章查询 Tag 和 Media。
         */
        List<Long> postIds = new ArrayList<>();
        postIds.add(techRow.getId());
        postIds.addAll(
                relatedRows.stream()
                        .map(PostFeedRow::getId)
                        .toList()
        );

        Map<Long, List<TagVO>> tagsByPostId =
                groupTags(
                        postMapper.selectTagsByPostIds(postIds)
                );

        Map<Long, List<MediaVO>> mediaByPostId =
                groupMedia(
                        postMapper.selectMediaByPostIds(postIds)
                );

        PostSummaryVO summary = toPostSummary(
                techRow,
                tagsByPostId.getOrDefault(
                        techRow.getId(),
                        List.of()
                ),
                mediaByPostId.getOrDefault(
                        techRow.getId(),
                        List.of()
                )
        );

        List<PostSummaryVO> relatedPosts =
                relatedRows.stream()
                        .map(row -> toPostSummary(
                                row,
                                tagsByPostId.getOrDefault(
                                        row.getId(),
                                        List.of()
                                ),
                                mediaByPostId.getOrDefault(
                                        row.getId(),
                                        List.of()
                                )
                        ))
                        .toList();

        return new TechDetailVO(
                summary.id(),
                summary.type(),
                summary.slug(),
                summary.title(),
                summary.summary(),
                techRow.getContent(),
                summary.author(),
                summary.category(),
                summary.tags(),
                summary.cover(),
                summary.images(),
                summary.createdAt(),
                summary.updatedAt(),
                summary.publishedAt(),
                summary.readingTime(),
                summary.status(),
                summary.likeCount(),
                summary.commentCount(),
                summary.version(),
                techRow.getContentFormat(),
                relatedPosts
        );
    }

    /**
     * 获取公开 MOMENT 列表。
     *
     * <p>MOMENT 不需要 Category、Tag 和 Cover，因此只批量加载媒体图片。</p>
     */
    @Override
    public PageVO<PostSummaryVO> getPublicMoments(
            Integer year,
            Integer month,
            int page,
            int pageSize
    ) {
        long total = postMapper.countPublicMoments(
                year,
                month
        );

        if (total == 0) {
            return new PageVO<>(
                    List.of(),
                    page,
                    pageSize,
                    0,
                    0,
                    false
            );
        }

        long offset = (long) (page - 1) * pageSize;

        List<PostFeedRow> momentRows =
                postMapper.selectPublicMoments(
                        year,
                        month,
                        offset,
                        pageSize
                );

        long totalPages =
                calculateTotalPages(total, pageSize);

        if (momentRows.isEmpty()) {
            return new PageVO<>(
                    List.of(),
                    page,
                    pageSize,
                    total,
                    totalPages,
                    false
            );
        }

        List<Long> postIds = momentRows.stream()
                .map(PostFeedRow::getId)
                .toList();

        Map<Long, List<MediaVO>> mediaByPostId =
                groupMedia(
                        postMapper.selectMediaByPostIds(postIds)
                );

        List<PostSummaryVO> items = momentRows.stream()
                .map(row -> toPostSummary(
                        row,
                        List.of(),
                        mediaByPostId.getOrDefault(
                                row.getId(),
                                List.of()
                        )
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

    /**
     * 根据 ID 获取公开 MOMENT 详情。
     */
    @Override
    public MomentDetailVO getPublicMoment(Long id) {
        PostFeedRow momentRow =
                postMapper.selectPublicMomentById(id);

        if (momentRow == null) {
            throw new ResourceNotFoundException(
                    "说说不存在"
            );
        }

        Map<Long, List<MediaVO>> mediaByPostId =
                groupMedia(
                        postMapper.selectMediaByPostIds(
                                List.of(momentRow.getId())
                        )
                );

        PostSummaryVO summary = toPostSummary(
                momentRow,
                List.of(),
                mediaByPostId.getOrDefault(
                        momentRow.getId(),
                        List.of()
                )
        );

        return new MomentDetailVO(
                summary.id(),
                summary.type(),
                summary.slug(),
                summary.title(),
                summary.summary(),
                momentRow.getContent(),
                summary.author(),
                summary.category(),
                summary.tags(),
                summary.cover(),
                summary.images(),
                summary.createdAt(),
                summary.updatedAt(),
                summary.publishedAt(),
                summary.readingTime(),
                summary.status(),
                summary.likeCount(),
                summary.commentCount(),
                summary.version(),
                momentRow.getContentFormat()
        );
    }

    private PostSummaryVO toPostSummary(
            PostFeedRow row,
            List<TagVO> tags,
            List<MediaVO> media
    ) {
        boolean tech = TECH.equals(row.getType());
        boolean moment = MOMENT.equals(row.getType());

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
                    !Boolean.TRUE.equals(
                            row.getCategoryDeleted()
                    )
            );
        }

        MediaVO cover = null;

        if (tech) {
            cover = media.stream()
                    .filter(item ->
                            COVER.equals(item.usageType())
                    )
                    .findFirst()
                    .orElse(null);
        }

        List<MediaVO> images;

        if (moment) {
            images = media.stream()
                    .filter(item ->
                            CONTENT.equals(item.usageType())
                    )
                    .toList();
        } else {
            images = List.of();
        }

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

                // Visitor 只能查到已发布内容。
                "PUBLISHED",

                row.getLikeCount(),
                row.getCommentCount(),

                // Visitor 不需要获得乐观锁版本。
                null
        );
    }

    private Map<Long, List<TagVO>> groupTags(
            List<PostTagRow> rows
    ) {
        Map<Long, List<TagVO>> result =
                new HashMap<>();

        for (PostTagRow row : rows) {
            TagVO tag = new TagVO(
                    String.valueOf(row.getTagId()),
                    row.getTagName(),
                    row.getTagSlug(),
                    !Boolean.TRUE.equals(
                            row.getTagDeleted()
                    )
            );

            result.computeIfAbsent(
                    row.getPostId(),
                    key -> new ArrayList<>()
            ).add(tag);
        }

        return result;
    }

    private Map<Long, List<MediaVO>> groupMedia(
            List<PostMediaRow> rows
    ) {
        Map<Long, List<MediaVO>> result =
                new HashMap<>();

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

            result.computeIfAbsent(
                    row.getPostId(),
                    key -> new ArrayList<>()
            ).add(media);
        }

        return result;
    }

    private long calculateTotalPages(
            long total,
            int pageSize
    ) {
        return (total + pageSize - 1) / pageSize;
    }

    /**
     * 空字符串不作为筛选条件，统一转换为 null。
     */
    private String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }

        return slug.trim();
    }

    private Instant toInstant(LocalDateTime time) {
        if (time == null) {
            return null;
        }

        return time.toInstant(ZoneOffset.UTC);
    }

    /**
     * 公开 Post 列表的公共分页查询。
     *
     * 首页和 TECH 索引都复用该方法。
     */
    private PageVO<PostSummaryVO> getPublicPosts(
            String type,
            String categorySlug,
            String tagSlug,
            int page,
            int pageSize
    ) {
        /*
         * 1. 查询符合筛选条件的总数。
         */
        long total = postMapper.countPublicFeed(
                type,
                categorySlug,
                tagSlug
        );

        if (total == 0) {
            return new PageVO<>(
                    List.of(),
                    page,
                    pageSize,
                    0,
                    0,
                    false
            );
        }

        /*
         * 2. 计算物理分页偏移量。
         */
        long offset = (long) (page - 1) * pageSize;

        /*
         * 3. 只分页查询 Post 主结果。
         */
        List<PostFeedRow> postRows =
                postMapper.selectPublicFeed(
                        type,
                        categorySlug,
                        tagSlug,
                        offset,
                        pageSize
                );

        long totalPages =
                calculateTotalPages(total, pageSize);

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

        /*
         * 4. 提取当前页 Post ID。
         */
        List<Long> postIds = postRows.stream()
                .map(PostFeedRow::getId)
                .toList();

        /*
         * 5. 批量查询当前页标签和媒体。
         */
        Map<Long, List<TagVO>> tagsByPostId =
                groupTags(
                        postMapper.selectTagsByPostIds(postIds)
                );

        Map<Long, List<MediaVO>> mediaByPostId =
                groupMedia(
                        postMapper.selectMediaByPostIds(postIds)
                );

        /*
         * 6. 组装当前页结果。
         */
        List<PostSummaryVO> items = postRows.stream()
                .map(row -> toPostSummary(
                        row,
                        tagsByPostId.getOrDefault(
                                row.getId(),
                                List.of()
                        ),
                        mediaByPostId.getOrDefault(
                                row.getId(),
                                List.of()
                        )
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
}
