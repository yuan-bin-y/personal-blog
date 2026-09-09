package com.byy.blogprojectbackend.post.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.util.SlugUtils;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.CreateMomentDTO;
import com.byy.blogprojectbackend.post.dto.CreateTechPostDTO;
import com.byy.blogprojectbackend.post.dto.MediaInputDTO;
import com.byy.blogprojectbackend.post.dto.UpdateMomentDTO;
import com.byy.blogprojectbackend.post.dto.UpdateTechPostDTO;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.entity.PostMedia;
import com.byy.blogprojectbackend.post.enums.PostContentFormat;
import com.byy.blogprojectbackend.post.enums.PostMediaUsage;
import com.byy.blogprojectbackend.post.enums.PostStatus;
import com.byy.blogprojectbackend.post.enums.PostType;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostMediaRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostTagRow;
import com.byy.blogprojectbackend.post.service.OwnerPostService;
import com.byy.blogprojectbackend.post.service.PostVersionSnapshotService;
import com.byy.blogprojectbackend.post.vo.CategoryVO;
import com.byy.blogprojectbackend.post.vo.MediaVO;
import com.byy.blogprojectbackend.post.vo.MomentDetailVO;
import com.byy.blogprojectbackend.post.vo.PostAuthorVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.post.vo.TagVO;
import com.byy.blogprojectbackend.post.vo.TechDetailVO;
import com.byy.blogprojectbackend.search.event.PublishedPostChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Owner 内容写入实现。
 *
 * <p>Post 主表、标签关系和媒体关系在同一事务内变更，任何一步失败都会整体回滚。</p>
 */
@Service
@RequiredArgsConstructor
public class OwnerPostServiceImpl implements OwnerPostService {

    private final PostMapper postMapper;
    private final IdGenerator idGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final PostVersionSnapshotService snapshotService;

    @Override
    public PageVO<PostSummaryVO> list(String type, String status, int page, int pageSize) {
        long total = postMapper.countOwnerPosts(type, status);
        List<PostFeedRow> rows = postMapper.selectOwnerPosts(
                type,
                status,
                (long) (page - 1) * pageSize,
                pageSize
        );
        Associations associations = associations(rows);
        List<PostSummaryVO> items = rows.stream()
                .map(row -> summary(
                        row,
                        associations.tags(row.getId()),
                        associations.media(row.getId())
                ))
                .toList();
        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageVO<>(items, page, pageSize, total, totalPages, page < totalPages);
    }

    @Override
    @Transactional
    public TechDetailVO createTech(CreateTechPostDTO dto, Long ownerId) {
        Long categoryId = parseId(dto.categoryId(), "categoryId");
        List<Long> tagIds = parseUniqueIds(dto.tagIds(), "tagIds");
        validateTaxonomy(categoryId, tagIds);

        Long id = idGenerator.nextId();
        Post post = base(
                id,
                PostType.TECH,
                dto.content(),
                dto.contentFormat(),
                dto.status(),
                ownerId
        );
        post.setSlug(SlugUtils.normalize(dto.title(), "tech") + "-" + id);
        post.setTitle(dto.title().trim());
        post.setSummary(dto.summary().trim());
        post.setCategoryId(categoryId);
        post.setReadingTimeMinutes(readingTime(dto.content()));
        postMapper.insert(post);

        replaceTags(id, tagIds);
        replaceMedia(
                id,
                dto.cover() == null ? List.of() : List.of(dto.cover()),
                PostMediaUsage.COVER
        );

        TechDetailVO result = getTech(id);
        publishSearchChange(id);
        return result;
    }

    @Override
    public TechDetailVO getTech(Long id) {
        PostFeedRow row = requireRow(id, PostType.TECH);
        Associations associations = associations(List.of(row));
        PostSummaryVO summary = summary(
                row,
                associations.tags(id),
                associations.media(id)
        );
        return techDetail(summary, row.getContent(), row.getContentFormat());
    }

    @Override
    @Transactional
    public TechDetailVO updateTech(Long id, UpdateTechPostDTO dto, Long ownerId) {
        PostFeedRow old = requireRow(id, PostType.TECH);
        Long categoryId = parseId(dto.categoryId(), "categoryId");
        List<Long> tagIds = parseUniqueIds(dto.tagIds(), "tagIds");
        validateTaxonomy(categoryId, tagIds);

        Post post = base(
                id,
                PostType.TECH,
                dto.content(),
                dto.contentFormat(),
                dto.status(),
                ownerId
        );
        post.setSlug(old.getSlug());
        post.setTitle(dto.title().trim());
        post.setSummary(dto.summary().trim());
        post.setCategoryId(categoryId);
        post.setReadingTimeMinutes(readingTime(dto.content()));
        post.setPublishedAt(publicationTime(old, dto.status()));

        snapshotService.capture(postMapper.selectById(id), ownerId);
        update(post, dto.version());
        replaceTags(id, tagIds);
        replaceMedia(
                id,
                dto.cover() == null ? List.of() : List.of(dto.cover()),
                PostMediaUsage.COVER
        );

        TechDetailVO result = getTech(id);
        publishSearchChange(id);
        return result;
    }

    @Override
    @Transactional
    public void deleteTech(Long id, int version, Long ownerId) {
        delete(id, PostType.TECH, version, ownerId);
    }

    @Override
    @Transactional
    public MomentDetailVO createMoment(CreateMomentDTO dto, Long ownerId) {
        Long id = idGenerator.nextId();
        Post post = base(
                id,
                PostType.MOMENT,
                dto.content(),
                PostContentFormat.PLAIN_TEXT.code(),
                dto.status(),
                ownerId
        );
        postMapper.insert(post);
        replaceMedia(id, dto.images(), PostMediaUsage.CONTENT);

        MomentDetailVO result = getMoment(id);
        publishSearchChange(id);
        return result;
    }

    @Override
    public MomentDetailVO getMoment(Long id) {
        PostFeedRow row = requireRow(id, PostType.MOMENT);
        Associations associations = associations(List.of(row));
        PostSummaryVO summary = summary(row, List.of(), associations.media(id));
        return momentDetail(summary, row.getContentFormat());
    }

    @Override
    @Transactional
    public MomentDetailVO updateMoment(Long id, UpdateMomentDTO dto, Long ownerId) {
        PostFeedRow old = requireRow(id, PostType.MOMENT);
        Post post = base(
                id,
                PostType.MOMENT,
                dto.content(),
                PostContentFormat.PLAIN_TEXT.code(),
                dto.status(),
                ownerId
        );
        post.setPublishedAt(publicationTime(old, dto.status()));

        snapshotService.capture(postMapper.selectById(id), ownerId);
        update(post, dto.version());
        replaceMedia(id, dto.images(), PostMediaUsage.CONTENT);

        MomentDetailVO result = getMoment(id);
        publishSearchChange(id);
        return result;
    }

    @Override
    @Transactional
    public void deleteMoment(Long id, int version, Long ownerId) {
        delete(id, PostType.MOMENT, version, ownerId);
    }

    private Post base(
            Long id,
            PostType type,
            String content,
            String format,
            String status,
            Long ownerId
    ) {
        Post post = new Post();
        post.setId(id);
        post.setType(type.code());
        post.setContent(content.trim());
        post.setContentFormat(format);
        post.setStatus(status);
        post.setPublishedAt(
                PostStatus.PUBLISHED.matches(status)
                        ? LocalDateTime.now(Clock.systemUTC())
                        : null
        );
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedBy(ownerId);
        post.setUpdatedBy(ownerId);
        return post;
    }

    private LocalDateTime publicationTime(PostFeedRow old, String status) {
        if (PostStatus.DRAFT.matches(status)) {
            return null;
        }
        return old.getPublishedAt() == null
                ? LocalDateTime.now(Clock.systemUTC())
                : old.getPublishedAt();
    }

    private void update(Post post, int version) {
        if (postMapper.updateOwnerPost(post, version) != 1) {
            throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
        }
    }

    private void delete(Long id, PostType type, int version, Long ownerId) {
        requireRow(id, type);
        snapshotService.capture(postMapper.selectById(id), ownerId);
        if (postMapper.softDeleteOwnerPost(id, type.code(), version, ownerId) != 1) {
            throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
        }
        postMapper.softDeletePostMedia(id);
        publishSearchChange(id);
    }

    private void validateTaxonomy(Long categoryId, List<Long> tagIds) {
        if (postMapper.countActiveCategory(categoryId) != 1) {
            throw new IllegalArgumentException("categoryId 对应的分类不存在或已停用");
        }
        if (!tagIds.isEmpty() && postMapper.countActiveTags(tagIds) != tagIds.size()) {
            throw new IllegalArgumentException("tagIds 中包含不存在或已停用的标签");
        }
    }

    private void replaceTags(Long postId, List<Long> tagIds) {
        postMapper.deletePostTags(postId);
        for (Long tagId : tagIds) {
            postMapper.insertPostTag(postId, tagId);
        }
    }

    private void replaceMedia(Long postId, List<MediaInputDTO> media, PostMediaUsage usage) {
        postMapper.softDeletePostMedia(postId);
        for (int index = 0; index < media.size(); index++) {
            MediaInputDTO input = media.get(index);
            PostMedia row = new PostMedia();
            row.setId(idGenerator.nextId());
            row.setPostId(postId);
            row.setUsageType(usage.code());
            row.setMediaType(input.mediaType());
            row.setUrl(input.src().trim());
            row.setPosterUrl(trim(input.poster()));
            row.setAltText(trim(input.alt()));
            row.setWidth(input.width());
            row.setHeight(input.height());
            row.setSortOrder(input.sortOrder() == null ? index : input.sortOrder());
            postMapper.insertPostMedia(row);
        }
    }

    private PostFeedRow requireRow(Long id, PostType type) {
        PostFeedRow row = postMapper.selectOwnerPostById(id, type.code());
        if (row == null) {
            throw new ResourceNotFoundException(type.label() + "不存在");
        }
        return row;
    }

    private Associations associations(List<PostFeedRow> rows) {
        if (rows.isEmpty()) {
            return new Associations(Map.of(), Map.of());
        }

        List<Long> ids = rows.stream().map(PostFeedRow::getId).toList();
        Map<Long, List<TagVO>> tags = new HashMap<>();
        for (PostTagRow row : postMapper.selectTagsByPostIds(ids)) {
            tags.computeIfAbsent(row.getPostId(), ignored -> new ArrayList<>())
                    .add(new TagVO(
                            String.valueOf(row.getTagId()),
                            row.getTagName(),
                            row.getTagSlug(),
                            !Boolean.TRUE.equals(row.getTagDeleted())
                    ));
        }

        Map<Long, List<MediaVO>> media = new HashMap<>();
        for (PostMediaRow row : postMapper.selectMediaByPostIds(ids)) {
            media.computeIfAbsent(row.getPostId(), ignored -> new ArrayList<>())
                    .add(new MediaVO(
                            String.valueOf(row.getId()),
                            row.getUsageType(),
                            row.getMediaType(),
                            row.getUrl(),
                            row.getPosterUrl(),
                            row.getAltText(),
                            row.getWidth(),
                            row.getHeight(),
                            row.getSortOrder()
                    ));
        }
        return new Associations(tags, media);
    }

    private PostSummaryVO summary(PostFeedRow row, List<TagVO> tags, List<MediaVO> media) {
        boolean tech = PostType.TECH.matches(row.getType());
        PostAuthorVO author = new PostAuthorVO(
                String.valueOf(row.getAuthorUserId()),
                row.getAuthorName(),
                row.getAuthorAvatar()
        );
        CategoryVO category = tech
                ? new CategoryVO(
                        String.valueOf(row.getCategoryId()),
                        row.getCategoryName(),
                        row.getCategorySlug(),
                        row.getCategoryDescription(),
                        !Boolean.TRUE.equals(row.getCategoryDeleted())
                )
                : null;
        MediaVO cover = tech
                ? media.stream()
                        .filter(item -> PostMediaUsage.COVER.matches(item.usageType()))
                        .findFirst()
                        .orElse(null)
                : null;
        List<MediaVO> images = tech
                ? List.of()
                : media.stream()
                        .filter(item -> PostMediaUsage.CONTENT.matches(item.usageType()))
                        .toList();

        return new PostSummaryVO(
                String.valueOf(row.getId()),
                row.getType(),
                tech ? row.getSlug() : null,
                tech ? row.getTitle() : null,
                tech ? row.getSummary() : null,
                tech ? null : row.getContent(),
                author,
                category,
                tech ? tags : List.of(),
                cover,
                images,
                toInstant(row.getCreatedAt()),
                toInstant(row.getUpdatedAt()),
                toInstant(row.getPublishedAt()),
                tech ? row.getReadingTime() : null,
                row.getStatus(),
                row.getLikeCount(),
                false,
                row.getCommentCount(),
                row.getVersion()
        );
    }

    private TechDetailVO techDetail(PostSummaryVO summary, String content, String format) {
        return new TechDetailVO(
                summary.id(), summary.type(), summary.slug(), summary.title(), summary.summary(), content,
                summary.author(), summary.category(), summary.tags(), summary.cover(), summary.images(),
                summary.createdAt(), summary.updatedAt(), summary.publishedAt(), summary.readingTime(),
                summary.status(), summary.likeCount(), summary.commentCount(), summary.version(), format, List.of()
        );
    }

    private MomentDetailVO momentDetail(PostSummaryVO summary, String format) {
        return new MomentDetailVO(
                summary.id(), summary.type(), summary.slug(), summary.title(), summary.summary(), summary.content(),
                summary.author(), summary.category(), summary.tags(), summary.cover(), summary.images(),
                summary.createdAt(), summary.updatedAt(), summary.publishedAt(), summary.readingTime(),
                summary.status(), summary.likeCount(), summary.commentCount(), summary.version(), format
        );
    }

    private int readingTime(String content) {
        return Math.max(1, (content.codePointCount(0, content.length()) + 499) / 500);
    }

    private Long parseId(String value, String field) {
        final long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(field + " 必须是正整数");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(field + " 必须是正整数");
        }
        return id;
    }

    private List<Long> parseUniqueIds(List<String> values, String field) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String value : values) {
            ids.add(parseId(value, field));
        }
        if (ids.size() != values.size()) {
            throw new IllegalArgumentException(field + " 不能重复");
        }
        return List.copyOf(ids);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private void publishSearchChange(Long postId) {
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
    }

    private record Associations(
            Map<Long, List<TagVO>> tags,
            Map<Long, List<MediaVO>> media
    ) {
        List<TagVO> tags(Long id) {
            return tags.getOrDefault(id, List.of());
        }

        List<MediaVO> media(Long id) {
            return media.getOrDefault(id, List.of());
        }
    }
}
