package com.byy.blogprojectbackend.media.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.media.entity.MediaAsset;
import com.byy.blogprojectbackend.media.enums.MediaStatus;
import com.byy.blogprojectbackend.media.enums.MediaType;
import com.byy.blogprojectbackend.media.enums.MediaUsageType;
import com.byy.blogprojectbackend.media.exception.MediaStorageException;
import com.byy.blogprojectbackend.media.mapper.MediaAssetMapper;
import com.byy.blogprojectbackend.media.service.MediaAssetStateService;
import com.byy.blogprojectbackend.media.service.MediaService;
import com.byy.blogprojectbackend.media.storage.ObjectStorageService;
import com.byy.blogprojectbackend.media.validation.DetectedMedia;
import com.byy.blogprojectbackend.media.validation.MediaFileValidator;
import com.byy.blogprojectbackend.media.vo.MediaAssetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaAssetMapper mediaAssetMapper;
    private final MediaAssetStateService stateService;
    private final ObjectStorageService storageService;
    private final MediaFileValidator fileValidator;
    private final IdGenerator idGenerator;

    @Override
    public MediaAssetVO upload(
            MultipartFile file,
            MediaUsageType usageType,
            Long ownerId
    ) {
        DetectedMedia detected = fileValidator.validate(file, usageType);
        Long mediaId = idGenerator.nextId();
        String objectKey = buildObjectKey(mediaId, detected.extension());

        MediaAsset asset = new MediaAsset();
        asset.setId(mediaId);
        asset.setMediaType(detected.mediaType().code());
        asset.setUsageType(usageType.code());
        asset.setStorageProvider(storageService.provider());
        asset.setBucketName(storageService.bucketName());
        asset.setObjectKey(objectKey);
        asset.setOriginalName(normalizeOriginalName(file.getOriginalFilename()));
        asset.setContentType(detected.contentType());
        asset.setSizeBytes(file.getSize());
        asset.setWidth(detected.width());
        asset.setHeight(detected.height());
        asset.setDurationSeconds(detected.durationSeconds());
        asset.setStatus(MediaStatus.UPLOADING.code());
        asset.setCreatedBy(ownerId);
        asset.setDeleted(false);
        stateService.createUploading(asset);

        try (InputStream input = file.getInputStream()) {
            storageService.upload(
                    objectKey,
                    input,
                    file.getSize(),
                    detected.contentType()
            );
        } catch (IOException | RuntimeException exception) {
            stateService.markUploadFailed(mediaId, exception.getMessage());
            safeDelete(objectKey);
            throw new MediaStorageException("媒体上传失败", exception);
        }

        try {
            String url = storageService.publicUrl(objectKey);
            return toVO(stateService.markActive(mediaId, url));
        } catch (RuntimeException exception) {
            stateService.markUploadFailed(mediaId, exception.getMessage());
            safeDelete(objectKey);
            throw exception;
        }
    }

    @Override
    public PageVO<MediaAssetVO> list(
            MediaType mediaType,
            MediaUsageType usageType,
            int page,
            int pageSize
    ) {
        String type = mediaType == null ? null : mediaType.code();
        String usage = usageType == null ? null : usageType.code();
        long total = mediaAssetMapper.countActive(type, usage);
        long totalPages = (total + pageSize - 1) / pageSize;

        if (total == 0) {
            return new PageVO<>(java.util.List.of(), page, pageSize, 0, 0, false);
        }

        long offset = (long) (page - 1) * pageSize;
        java.util.List<MediaAssetVO> items = mediaAssetMapper
                .selectActivePage(type, usage, offset, pageSize)
                .stream()
                .map(this::toVO)
                .toList();
        return new PageVO<>(items, page, pageSize, total, totalPages, page < totalPages);
    }

    @Override
    public void delete(Long mediaId, Long ownerId) {
        MediaAsset asset = stateService.prepareDelete(mediaId, ownerId);
        if (!storageService.provider().equals(asset.getStorageProvider())) {
            stateService.markDeleteFailed(mediaId, "当前存储 Provider 与媒体记录不一致");
            throw new ResourceConflictException("当前存储配置无法删除该媒体");
        }

        try {
            storageService.delete(asset.getObjectKey());
            stateService.markDeleted(mediaId, ownerId);
        } catch (RuntimeException exception) {
            stateService.markDeleteFailed(mediaId, exception.getMessage());
            throw new MediaStorageException("媒体删除失败，已记录等待重试", exception);
        }
    }

    private String buildObjectKey(Long mediaId, String extension) {
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        return "binspace/%d/%02d/%s.%s".formatted(
                date.getYear(),
                date.getMonthValue(),
                mediaId,
                extension
        );
    }

    private String normalizeOriginalName(String originalName) {
        String value = originalName == null || originalName.isBlank() ? "unnamed" : originalName;
        value = value.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1).trim();
        if (value.isBlank()) {
            value = "unnamed";
        }
        return value.length() <= 255 ? value : value.substring(value.length() - 255);
    }

    private void safeDelete(String objectKey) {
        try {
            storageService.delete(objectKey);
        } catch (RuntimeException ignored) {
            // UPLOAD_FAILED 已保留 objectKey，后续清理任务仍可重试。
        }
    }

    private MediaAssetVO toVO(MediaAsset asset) {
        return new MediaAssetVO(
                String.valueOf(asset.getId()),
                asset.getMediaType(),
                asset.getUsageType(),
                asset.getUrl(),
                asset.getOriginalName(),
                asset.getContentType(),
                asset.getSizeBytes(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getDurationSeconds(),
                asset.getCreatedAt() == null
                        ? null
                        : asset.getCreatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
