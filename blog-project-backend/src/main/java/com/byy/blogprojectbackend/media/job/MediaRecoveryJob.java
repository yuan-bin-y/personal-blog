package com.byy.blogprojectbackend.media.job;

import com.byy.blogprojectbackend.media.entity.MediaAsset;
import com.byy.blogprojectbackend.media.enums.MediaStatus;
import com.byy.blogprojectbackend.media.mapper.MediaAssetMapper;
import com.byy.blogprojectbackend.media.service.MediaAssetStateService;
import com.byy.blogprojectbackend.media.service.MediaService;
import com.byy.blogprojectbackend.media.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 让 OSS/MySQL 中途失败的媒体最终恢复一致；每次只处理小批量。 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.media", name = "retry-enabled", havingValue = "true", matchIfMissing = true)
public class MediaRecoveryJob {

    private static final Logger log = LoggerFactory.getLogger(MediaRecoveryJob.class);
    private final MediaAssetMapper mediaAssetMapper;
    private final MediaAssetStateService stateService;
    private final MediaService mediaService;
    private final ObjectStorageService storageService;

    @Scheduled(fixedDelayString = "${app.media.retry-delay-ms:60000}")
    public void retryFailures() {
        for (MediaAsset asset : mediaAssetMapper.selectRetryableFailures(50)) {
            if (!storageService.provider().equals(asset.getStorageProvider())) {
                continue;
            }
            try {
                if (MediaStatus.DELETE_FAILED.matches(asset.getStatus())) {
                    mediaService.delete(asset.getId(), deleteOperator(asset));
                } else if (MediaStatus.UPLOAD_FAILED.matches(asset.getStatus())) {
                    cleanupFailedUpload(asset);
                }
            } catch (RuntimeException exception) {
                log.warn("媒体恢复任务执行失败，mediaId={}", asset.getId(), exception);
            }
        }
    }

    private void cleanupFailedUpload(MediaAsset asset) {
        Long operator = deleteOperator(asset);
        if (!stateService.prepareFailedUploadCleanup(asset.getId(), operator)) {
            return;
        }
        try {
            storageService.delete(asset.getObjectKey());
            stateService.markDeleted(asset.getId(), operator);
        } catch (RuntimeException exception) {
            stateService.markDeleteFailed(asset.getId(), exception.getMessage());
            throw exception;
        }
    }

    private Long deleteOperator(MediaAsset asset) {
        return asset.getDeletedBy() == null ? asset.getCreatedBy() : asset.getDeletedBy();
    }
}
