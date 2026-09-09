package com.byy.blogprojectbackend.post.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.result.ApiErrorCode;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.*;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.entity.PostAutosave;
import com.byy.blogprojectbackend.post.entity.PostMedia;
import com.byy.blogprojectbackend.post.enums.PostStatus;
import com.byy.blogprojectbackend.post.enums.PostType;
import com.byy.blogprojectbackend.post.mapper.PostAdvancedMapper;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostVersionRow;
import com.byy.blogprojectbackend.post.model.PostSnapshot;
import com.byy.blogprojectbackend.post.service.OwnerAdvancedPostService;
import com.byy.blogprojectbackend.post.service.OwnerPostService;
import com.byy.blogprojectbackend.post.service.PostVersionSnapshotService;
import com.byy.blogprojectbackend.post.vo.*;
import com.byy.blogprojectbackend.media.exception.MediaPayloadTooLargeException;
import com.byy.blogprojectbackend.media.exception.UnsupportedMediaTypeException;
import com.byy.blogprojectbackend.search.event.PublishedPostChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OwnerAdvancedPostServiceImpl implements OwnerAdvancedPostService {
    private static final int MAX_MARKDOWN_BYTES = 2 * 1024 * 1024;
    private static final int MAX_AUTOSAVE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> TECH_AUTOSAVE_FIELDS = Set.of(
            "title", "summary", "content", "contentFormat", "categoryId", "tagIds", "cover", "status"
    );
    private static final Set<String> MOMENT_AUTOSAVE_FIELDS = Set.of("content", "images", "status");

    private final PostMapper postMapper;
    private final PostAdvancedMapper advancedMapper;
    private final OwnerPostService ownerPostService;
    private final OwnerAdvancedPostItemService itemService;
    private final PostVersionSnapshotService snapshotService;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PostSummaryVO schedule(Long postId, SchedulePostDTO dto, Long ownerId) {
        Post post = require(postId, false);
        requireVersion(post, dto.version());
        if (PostStatus.PUBLISHED.matches(post.getStatus())) {
            throw new IllegalArgumentException("已发布内容不能直接改为定时发布，请先撤回为草稿");
        }
        snapshotService.capture(post, ownerId);
        LocalDateTime publishAt = LocalDateTime.ofInstant(dto.publishAt(), ZoneOffset.UTC);
        if (advancedMapper.schedule(postId, publishAt, dto.version(), ownerId) != 1) {
            throw versionConflict();
        }
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
        return summary(postId);
    }

    @Override
    @Transactional
    public PostSummaryVO cancelSchedule(Long postId, int version, Long ownerId) {
        Post post = require(postId, false);
        requireVersion(post, version);
        if (!PostStatus.SCHEDULED.matches(post.getStatus())) {
            throw new IllegalArgumentException("只有定时发布内容可以取消定时");
        }
        snapshotService.capture(post, ownerId);
        if (advancedMapper.cancelSchedule(postId, version, ownerId) != 1) {
            throw versionConflict();
        }
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
        return summary(postId);
    }

    @Override
    @Transactional
    public AutosaveVO autosave(Long postId, AutosavePostDTO dto, Long ownerId) {
        Post post = require(postId, false);
        requireVersion(post, dto.baseVersion());
        if (!post.getType().equals(dto.type())) {
            throw new IllegalArgumentException("自动保存类型与内容类型不一致");
        }
        validateAutosaveFields(dto);
        String payload = writeJson(dto.payload());
        if (payload.getBytes(StandardCharsets.UTF_8).length > MAX_AUTOSAVE_BYTES) {
            throw new IllegalArgumentException("自动保存内容不能超过 2MB");
        }
        PostAutosave autosave = new PostAutosave();
        autosave.setId(idGenerator.nextId());
        autosave.setPostId(postId);
        autosave.setPostType(dto.type());
        autosave.setPayloadJson(payload);
        autosave.setBaseVersion(dto.baseVersion());
        autosave.setUpdatedBy(ownerId);
        if (advancedMapper.saveAutosave(autosave) == 0) {
            throw versionConflict();
        }
        return new AutosaveVO(String.valueOf(postId), Instant.now(), dto.baseVersion());
    }

    @Override
    public PageVO<PostVersionVO> versions(Long postId, int page, int pageSize) {
        require(postId, false);
        long total = advancedMapper.countVersions(postId);
        List<PostVersionVO> items = advancedMapper.selectVersions(
                postId, (long) (page - 1) * pageSize, pageSize
        ).stream().map(this::versionVO).toList();
        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageVO<>(items, page, pageSize, total, totalPages, page < totalPages);
    }

    @Override
    public PostVersionDetailVO version(Long postId, Long versionId) {
        require(postId, false);
        PostVersionRow row = advancedMapper.selectVersion(postId, versionId);
        if (row == null) {
            throw new ResourceNotFoundException("历史版本不存在");
        }
        return new PostVersionDetailVO(
                String.valueOf(row.getId()), String.valueOf(row.getPostId()), row.getVersionNo(),
                row.getSummary(), toInstant(row.getCreatedAt()), row.getPostType(),
                snapshotService.read(row.getSnapshotJson())
        );
    }

    @Override
    @Transactional
    public PostSummaryVO restoreVersion(Long postId, Long versionId, RestoreVersionDTO dto, Long ownerId) {
        Post current = require(postId, false);
        requireVersion(current, dto.version());
        PostVersionRow row = advancedMapper.selectVersion(postId, versionId);
        if (row == null) {
            throw new ResourceNotFoundException("历史版本不存在");
        }
        PostSnapshot snapshot = snapshotService.read(row.getSnapshotJson());
        snapshotService.capture(current, ownerId);
        Post restored = snapshotPost(postId, snapshot);
        if (advancedMapper.restoreSnapshot(restored, dto.version(), ownerId) != 1) {
            throw versionConflict();
        }
        replaceAssociations(postId, snapshot);
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
        return summary(postId);
    }

    @Override
    public PageVO<PostSummaryVO> trash(String type, int page, int pageSize) {
        long total = advancedMapper.countTrash(type);
        List<PostSummaryVO> items = advancedMapper.selectTrash(
                type, (long) (page - 1) * pageSize, pageSize
        ).stream().map(this::trashSummary).toList();
        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageVO<>(items, page, pageSize, total, totalPages, page < totalPages);
    }

    @Override
    @Transactional
    public PostSummaryVO restoreTrash(Long postId, int version, Long ownerId) {
        Post post = require(postId, true);
        requireVersion(post, version);
        if (post.getSlug() != null && advancedMapper.countSlugExcluding(postId, post.getSlug()) > 0) {
            throw new ResourceConflictException("slug 已被其他文章占用");
        }
        if (advancedMapper.restoreTrash(postId, version, ownerId) != 1) {
            throw versionConflict();
        }
        advancedMapper.restoreLatestDeletedMedia(postId);
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
        return summary(postId);
    }

    @Override
    public BatchPostResultVO batchPublish(BatchPostDTO dto, Long ownerId) {
        validateBatch(dto);
        return batch(dto, item -> itemService.publishOne(parseId(item.postId()), item.version(), ownerId));
    }

    @Override
    public BatchPostResultVO batchDelete(BatchPostDTO dto, Long ownerId) {
        validateBatch(dto);
        return batch(dto, item -> itemService.deleteOne(parseId(item.postId()), item.version(), ownerId));
    }

    @Override
    @Transactional
    public TechDetailVO updateSlug(Long id, UpdateSlugDTO dto, Long ownerId) {
        Post post = require(id, false);
        if (!PostType.TECH.matches(post.getType())) {
            throw new ResourceNotFoundException(PostType.TECH.label() + "不存在");
        }
        requireVersion(post, dto.version());
        String slug = dto.slug().trim();
        if (advancedMapper.countSlugExcluding(id, slug) > 0) {
            throw new ResourceConflictException("slug 已存在");
        }
        snapshotService.capture(post, ownerId);
        if (advancedMapper.updateSlug(id, slug, dto.version(), ownerId) != 1) {
            throw versionConflict();
        }
        eventPublisher.publishEvent(new PublishedPostChangedEvent(id));
        return ownerPostService.getTech(id);
    }

    @Override
    public MarkdownImportVO importMarkdown(MultipartFile file) {
        validateMarkdownFile(file);
        String source = decodeUtf8(readBytes(file));
        ParsedMarkdown parsed = parseMarkdown(source);
        return new MarkdownImportVO(parsed.title(), parsed.content(), parsed.frontMatter());
    }

    private void validateAutosaveFields(AutosavePostDTO dto) {
        Set<String> allowed = PostType.TECH.matches(dto.type())
                ? TECH_AUTOSAVE_FIELDS
                : MOMENT_AUTOSAVE_FIELDS;
        Set<String> unknown = new LinkedHashSet<>(dto.payload().keySet());
        unknown.removeAll(allowed);
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("自动保存包含不可编辑字段：" + String.join(", ", unknown));
        }
    }

    private BatchPostResultVO batch(BatchPostDTO dto, BatchAction action) {
        List<String> succeeded = new ArrayList<>();
        List<BatchPostFailureVO> failed = new ArrayList<>();
        for (BatchPostItemDTO item : dto.items()) {
            try {
                action.run(item);
                succeeded.add(item.postId());
            } catch (RuntimeException exception) {
                failed.add(new BatchPostFailureVO(item.postId(), errorCode(exception), exception.getMessage()));
            }
        }
        return new BatchPostResultVO(List.copyOf(succeeded), List.copyOf(failed));
    }

    private void validateBatch(BatchPostDTO dto) {
        Set<String> ids = new HashSet<>();
        for (BatchPostItemDTO item : dto.items()) {
            if (!ids.add(item.postId())) {
                throw new IllegalArgumentException("批量请求中的 postId 不能重复");
            }
        }
    }

    private String errorCode(RuntimeException exception) {
        if (exception instanceof VersionConflictException) return ApiErrorCode.VERSION_CONFLICT.code();
        if (exception instanceof ResourceNotFoundException) return ApiErrorCode.RESOURCE_NOT_FOUND.code();
        if (exception instanceof ResourceConflictException) return ApiErrorCode.RESOURCE_CONFLICT.code();
        if (exception instanceof IllegalArgumentException) return ApiErrorCode.BAD_REQUEST.code();
        return ApiErrorCode.INTERNAL_ERROR.code();
    }

    private Post snapshotPost(Long id, PostSnapshot snapshot) {
        Post post = new Post();
        post.setId(id);
        post.setType(snapshot.type());
        post.setSlug(snapshot.slug());
        post.setTitle(snapshot.title());
        post.setSummary(snapshot.summary());
        post.setContent(snapshot.content());
        post.setContentFormat(snapshot.contentFormat());
        post.setCategoryId(snapshot.categoryId() == null ? null : parseId(snapshot.categoryId()));
        post.setReadingTimeMinutes(snapshot.readingTimeMinutes());
        post.setStatus(snapshot.status());
        post.setPublishedAt(snapshot.publishedAt() == null ? null
                : LocalDateTime.ofInstant(Instant.parse(snapshot.publishedAt()), ZoneOffset.UTC));
        return post;
    }

    private void replaceAssociations(Long postId, PostSnapshot snapshot) {
        postMapper.deletePostTags(postId);
        for (String tagId : snapshot.tagIds()) {
            postMapper.insertPostTag(postId, parseId(tagId));
        }
        postMapper.softDeletePostMedia(postId);
        for (PostSnapshot.SnapshotMedia input : snapshot.media()) {
            PostMedia media = new PostMedia();
            media.setId(idGenerator.nextId());
            media.setPostId(postId);
            media.setUsageType(input.usageType());
            media.setMediaType(input.mediaType());
            media.setUrl(input.src());
            media.setPosterUrl(input.poster());
            media.setAltText(input.alt());
            media.setWidth(input.width());
            media.setHeight(input.height());
            media.setSortOrder(input.sortOrder());
            postMapper.insertPostMedia(media);
        }
    }

    private PostSummaryVO summary(Long id) {
        Post post = require(id, false);
        return PostType.TECH.matches(post.getType())
                ? toSummary(ownerPostService.getTech(id))
                : toSummary(ownerPostService.getMoment(id));
    }

    private PostSummaryVO toSummary(TechDetailVO value) {
        return new PostSummaryVO(value.id(), value.type(), value.slug(), value.title(), value.summary(), null,
                value.author(), value.category(), value.tags(), value.cover(), value.images(), value.createdAt(),
                value.updatedAt(), value.publishedAt(), value.readingTime(), value.status(), value.likeCount(),
                false, value.commentCount(), value.version());
    }

    private PostSummaryVO toSummary(MomentDetailVO value) {
        return new PostSummaryVO(value.id(), value.type(), null, null, null, value.content(), value.author(),
                null, List.of(), null, value.images(), value.createdAt(), value.updatedAt(), value.publishedAt(),
                null, value.status(), value.likeCount(), false, value.commentCount(), value.version());
    }

    private PostSummaryVO trashSummary(PostFeedRow row) {
        PostAuthorVO author = new PostAuthorVO(String.valueOf(row.getAuthorUserId()), row.getAuthorName(), row.getAuthorAvatar());
        boolean tech = PostType.TECH.matches(row.getType());
        CategoryVO category = tech ? new CategoryVO(String.valueOf(row.getCategoryId()), row.getCategoryName(),
                row.getCategorySlug(), row.getCategoryDescription(), !Boolean.TRUE.equals(row.getCategoryDeleted())) : null;
        return new PostSummaryVO(String.valueOf(row.getId()), row.getType(), row.getSlug(), row.getTitle(),
                row.getSummary(), tech ? null : row.getContent(), author, category, List.of(), null, List.of(),
                toInstant(row.getCreatedAt()), toInstant(row.getUpdatedAt()), toInstant(row.getPublishedAt()),
                row.getReadingTime(), row.getStatus(), row.getLikeCount(), false, row.getCommentCount(), row.getVersion());
    }

    private PostVersionVO versionVO(PostVersionRow row) {
        return new PostVersionVO(String.valueOf(row.getId()), String.valueOf(row.getPostId()), row.getVersionNo(),
                row.getSummary(), toInstant(row.getCreatedAt()));
    }

    private Post require(Long id, boolean deleted) {
        Post post = postMapper.selectById(id);
        if (post == null || Boolean.TRUE.equals(post.getDeleted()) != deleted) {
            throw new ResourceNotFoundException("内容不存在");
        }
        return post;
    }

    private void requireVersion(Post post, int version) {
        if (!Objects.equals(post.getVersion(), version)) throw versionConflict();
    }

    private VersionConflictException versionConflict() {
        return new VersionConflictException("内容已被其他请求修改，请刷新后重试");
    }

    private Long parseId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("postId 格式不正确");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("自动保存内容无法序列化");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Markdown 文件读取失败");
        }
    }

    private String decodeUtf8(byte[] bytes) {
        try {
            CharBuffer chars = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return chars.toString();
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("Markdown 文件必须使用 UTF-8 编码");
        }
    }

    private void validateMarkdownFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Markdown 文件不能为空");
        if (file.getSize() > MAX_MARKDOWN_BYTES) throw new MediaPayloadTooLargeException("Markdown 文件不能超过 2MB");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (!name.endsWith(".md") && !name.endsWith(".markdown")) {
            throw new UnsupportedMediaTypeException("只支持 .md 或 .markdown 文件");
        }
        String type = Optional.ofNullable(file.getContentType()).orElse("");
        if (!type.isBlank() && !Set.of("text/markdown", "text/plain", "application/octet-stream").contains(type)) {
            throw new UnsupportedMediaTypeException("不支持的 Markdown Content-Type");
        }
    }

    private ParsedMarkdown parseMarkdown(String source) {
        Map<String, Object> frontMatter = new LinkedHashMap<>();
        String content = source;
        if (source.startsWith("---\n") || source.startsWith("---\r\n")) {
            String normalized = source.replace("\r\n", "\n");
            int end = normalized.indexOf("\n---\n", 4);
            if (end >= 0) {
                parseFrontMatter(normalized.substring(4, end), frontMatter);
                content = normalized.substring(end + 5).stripLeading();
            }
        }
        String title = Objects.toString(frontMatter.get("title"), "").trim();
        if (title.isEmpty()) {
            title = content.lines().filter(line -> line.startsWith("# "))
                    .map(line -> line.substring(2).trim()).findFirst().orElse("");
        }
        return new ParsedMarkdown(title, content, Map.copyOf(frontMatter));
    }

    private void parseFrontMatter(String raw, Map<String, Object> target) {
        for (String line : raw.split("\n")) {
            int separator = line.indexOf(':');
            if (separator <= 0) continue;
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (value.startsWith("[") && value.endsWith("]")) {
                List<String> values = Arrays.stream(value.substring(1, value.length() - 1).split(","))
                        .map(String::trim).map(this::unquote).filter(v -> !v.isEmpty()).toList();
                target.put(key, values);
            } else {
                target.put(key, unquote(value));
            }
        }
    }

    private String unquote(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    @FunctionalInterface
    private interface BatchAction {
        void run(BatchPostItemDTO item);
    }

    private record ParsedMarkdown(String title, String content, Map<String, Object> frontMatter) {
    }
}
