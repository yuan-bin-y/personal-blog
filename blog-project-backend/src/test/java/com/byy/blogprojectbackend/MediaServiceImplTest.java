package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.media.entity.MediaAsset;
import com.byy.blogprojectbackend.media.enums.MediaType;
import com.byy.blogprojectbackend.media.enums.MediaUsageType;
import com.byy.blogprojectbackend.media.exception.MediaStorageException;
import com.byy.blogprojectbackend.media.mapper.MediaAssetMapper;
import com.byy.blogprojectbackend.media.service.MediaAssetStateService;
import com.byy.blogprojectbackend.media.service.impl.MediaServiceImpl;
import com.byy.blogprojectbackend.media.storage.ObjectStorageService;
import com.byy.blogprojectbackend.media.validation.DetectedMedia;
import com.byy.blogprojectbackend.media.validation.MediaFileValidator;
import com.byy.blogprojectbackend.media.vo.MediaAssetVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaServiceImplTest {

    private final MediaAssetMapper mediaAssetMapper = mock(MediaAssetMapper.class);
    private final MediaAssetStateService stateService = mock(MediaAssetStateService.class);
    private final ObjectStorageService storageService = mock(ObjectStorageService.class);
    private final MediaFileValidator validator = mock(MediaFileValidator.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final MediaServiceImpl mediaService = new MediaServiceImpl(
            mediaAssetMapper,
            stateService,
            storageService,
            validator,
            idGenerator
    );

    @Test
    void upload_advancesDatabaseStateAroundStorageUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1, 2, 3}
        );
        DetectedMedia detected = new DetectedMedia(
                MediaType.IMAGE, "image/png", "png", 100, 100, null
        );
        MediaAsset active = activeAsset(301L);

        when(validator.validate(file, MediaUsageType.AVATAR)).thenReturn(detected);
        when(idGenerator.nextId()).thenReturn(301L);
        when(storageService.provider()).thenReturn("local");
        when(storageService.publicUrl(any())).thenReturn("http://localhost:8080/media/avatar.png");
        when(stateService.markActive(301L, "http://localhost:8080/media/avatar.png"))
                .thenReturn(active);

        MediaAssetVO result = mediaService.upload(file, MediaUsageType.AVATAR, 1L);

        assertEquals("301", result.id());
        verify(stateService).createUploading(any(MediaAsset.class));
        verify(storageService).upload(any(), any(), anyLong(), any());
        verify(stateService).markActive(301L, "http://localhost:8080/media/avatar.png");
    }

    @Test
    void list_returnsOnlyMapperActivePage() {
        MediaAsset active = activeAsset(301L);
        when(mediaAssetMapper.countActive("IMAGE", "AVATAR")).thenReturn(1L);
        when(mediaAssetMapper.selectActivePage("IMAGE", "AVATAR", 0L, 10))
                .thenReturn(List.of(active));

        PageVO<MediaAssetVO> result = mediaService.list(
                MediaType.IMAGE,
                MediaUsageType.AVATAR,
                1,
                10
        );

        assertEquals(1, result.total());
        assertEquals("301", result.items().get(0).id());
    }

    @Test
    void delete_removesStorageObjectThenMarksDatabaseDeleted() {
        MediaAsset asset = activeAsset(301L);
        asset.setStorageProvider("local");
        when(storageService.provider()).thenReturn("local");
        when(stateService.prepareDelete(301L, 1L)).thenReturn(asset);

        mediaService.delete(301L, 1L);

        verify(storageService).delete(asset.getObjectKey());
        verify(stateService).markDeleted(301L, 1L);
    }

    @Test
    void delete_whenStorageFails_recordsRecoverableFailure() {
        MediaAsset asset = activeAsset(301L);
        asset.setStorageProvider("local");
        when(storageService.provider()).thenReturn("local");
        when(stateService.prepareDelete(301L, 1L)).thenReturn(asset);
        org.mockito.Mockito.doThrow(new MediaStorageException("failed", new IOExceptionForTest()))
                .when(storageService).delete(asset.getObjectKey());

        assertThrows(MediaStorageException.class, () -> mediaService.delete(301L, 1L));

        verify(stateService).markDeleteFailed(301L, "failed");
    }

    private MediaAsset activeAsset(Long id) {
        MediaAsset asset = new MediaAsset();
        asset.setId(id);
        asset.setMediaType("IMAGE");
        asset.setUsageType("AVATAR");
        asset.setStorageProvider("local");
        asset.setObjectKey("binspace/2026/09/" + id + ".png");
        asset.setUrl("http://localhost:8080/media/" + id + ".png");
        asset.setOriginalName("avatar.png");
        asset.setContentType("image/png");
        asset.setSizeBytes(3L);
        asset.setWidth(100);
        asset.setHeight(100);
        asset.setDurationSeconds((BigDecimal) null);
        asset.setStatus("ACTIVE");
        asset.setCreatedAt(LocalDateTime.of(2026, 9, 7, 12, 0));
        return asset;
    }

    private static final class IOExceptionForTest extends RuntimeException {
    }
}
