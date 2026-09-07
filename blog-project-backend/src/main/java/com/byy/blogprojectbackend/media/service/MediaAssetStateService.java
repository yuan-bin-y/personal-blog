package com.byy.blogprojectbackend.media.service;

import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.media.entity.MediaAsset;
import com.byy.blogprojectbackend.media.mapper.MediaAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 只负责媒体数据库状态事务。OSS 调用位于另一个 Bean，避免误以为
 * Spring 的数据库事务可以回滚对象存储。
 */
@Service
@RequiredArgsConstructor
public class MediaAssetStateService {

    private final MediaAssetMapper mediaAssetMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaAsset createUploading(MediaAsset asset) {
        if (mediaAssetMapper.insert(asset) != 1) {
            throw new IllegalStateException("媒体记录创建失败");
        }
        return asset;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaAsset markActive(Long mediaId, String url) {
        if (mediaAssetMapper.markActive(mediaId, url) != 1) {
            throw new ResourceConflictException("媒体上传状态已经发生变化");
        }
        return require(mediaId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markUploadFailed(Long mediaId, String error) {
        mediaAssetMapper.markUploadFailed(mediaId, normalizeError(error));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaAsset prepareDelete(Long mediaId, Long ownerId) {
        MediaAsset asset = mediaAssetMapper.selectForUpdate(mediaId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new ResourceNotFoundException("媒体不存在");
        }
        if (!"ACTIVE".equals(asset.getStatus())
                && !"DELETE_FAILED".equals(asset.getStatus())) {
            throw new ResourceConflictException("媒体当前状态不允许删除");
        }
        if (asset.getUrl() != null && mediaAssetMapper.countReferences(asset.getUrl()) > 0) {
            throw new ResourceConflictException("媒体正在被空间内容引用，不能删除");
        }
        if (mediaAssetMapper.markDeleting(mediaId, ownerId) != 1) {
            throw new ResourceConflictException("媒体状态已经发生变化");
        }
        asset.setStatus("DELETING");
        asset.setDeletedBy(ownerId);
        return asset;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDeleted(Long mediaId, Long ownerId) {
        if (mediaAssetMapper.markDeleted(mediaId, ownerId) != 1) {
            throw new ResourceConflictException("媒体删除状态已经发生变化");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDeleteFailed(Long mediaId, String error) {
        mediaAssetMapper.markDeleteFailed(mediaId, normalizeError(error));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean prepareFailedUploadCleanup(Long mediaId, Long ownerId) {
        return mediaAssetMapper.markFailedUploadDeleting(mediaId, ownerId) == 1;
    }

    private MediaAsset require(Long mediaId) {
        MediaAsset asset = mediaAssetMapper.selectById(mediaId);
        if (asset == null) {
            throw new ResourceNotFoundException("媒体不存在");
        }
        return asset;
    }

    private String normalizeError(String error) {
        String value = error == null || error.isBlank() ? "unknown storage error" : error;
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
