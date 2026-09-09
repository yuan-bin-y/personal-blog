package com.byy.blogprojectbackend.post.service;

import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.entity.PostMedia;
import com.byy.blogprojectbackend.post.entity.PostVersion;
import com.byy.blogprojectbackend.post.enums.PostType;
import com.byy.blogprojectbackend.post.mapper.PostAdvancedMapper;
import com.byy.blogprojectbackend.post.model.PostSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneOffset;
import java.util.List;

/** Creates immutable snapshots inside the caller's database transaction. */
@Service
@RequiredArgsConstructor
public class PostVersionSnapshotService {
    private final PostAdvancedMapper advancedMapper;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public void capture(Post post, Long ownerId) {
        List<String> tagIds = advancedMapper.selectTagIds(post.getId()).stream()
                .map(String::valueOf).toList();
        List<PostSnapshot.SnapshotMedia> media = advancedMapper.selectActiveMedia(post.getId()).stream()
                .map(this::toSnapshotMedia).toList();

        PostSnapshot snapshot = new PostSnapshot(
                post.getType(), post.getSlug(), post.getTitle(), post.getSummary(), post.getContent(),
                post.getContentFormat(), post.getCategoryId() == null ? null : String.valueOf(post.getCategoryId()),
                post.getReadingTimeMinutes(), post.getStatus(),
                post.getPublishedAt() == null ? null : post.getPublishedAt().toInstant(ZoneOffset.UTC).toString(),
                tagIds, media
        );

        PostVersion version = new PostVersion();
        version.setId(idGenerator.nextId());
        version.setPostId(post.getId());
        version.setVersionNo(post.getVersion() + 1);
        version.setPostType(post.getType());
        version.setSummary(summary(post));
        version.setSnapshotJson(write(snapshot));
        version.setCreatedBy(ownerId);
        advancedMapper.insertVersion(version);
    }

    public PostSnapshot read(String json) {
        try {
            return objectMapper.readValue(json, PostSnapshot.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("历史版本快照无法解析", exception);
        }
    }

    private String write(PostSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JacksonException exception) {
            throw new IllegalStateException("历史版本快照无法保存", exception);
        }
    }

    private PostSnapshot.SnapshotMedia toSnapshotMedia(PostMedia media) {
        return new PostSnapshot.SnapshotMedia(
                media.getUsageType(), media.getMediaType(), media.getUrl(), media.getPosterUrl(),
                media.getAltText(), media.getWidth(), media.getHeight(), media.getSortOrder()
        );
    }

    private String summary(Post post) {
        String source = PostType.TECH.matches(post.getType()) ? post.getTitle() : post.getContent();
        String compact = source == null ? post.getType() : source.strip().replaceAll("\\s+", " ");
        int end = compact.offsetByCodePoints(0, Math.min(120, compact.codePointCount(0, compact.length())));
        return compact.substring(0, end);
    }
}
