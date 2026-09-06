package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.site.dto.UpdateAppearanceDTO;
import com.byy.blogprojectbackend.site.dto.UpdateMusicDTO;
import com.byy.blogprojectbackend.site.dto.UpdatePageMediaDTO;
import com.byy.blogprojectbackend.site.entity.SiteConfig;
import com.byy.blogprojectbackend.site.mapper.SiteConfigMapper;
import com.byy.blogprojectbackend.site.service.impl.OwnerSiteServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Owner 站点配置更新的核心行为测试。
 */
class OwnerSiteServiceImplTest {

    private static final Long OWNER_ID = 1L;
    private static final Integer CURRENT_VERSION = 3;

    private final SiteConfigMapper siteConfigMapper = mock(SiteConfigMapper.class);
    private final OwnerSiteServiceImpl ownerSiteService = new OwnerSiteServiceImpl(
            siteConfigMapper,
            new ObjectMapper()
    );

    @Test
    void updateMusic_whenAudioUrlIsBlank_returnsDisabledMusicAndLatestVersion() {
        SiteConfig current = config(CURRENT_VERSION);
        SiteConfig updated = config(CURRENT_VERSION + 1);
        updated.setMusicAudioUrl(" ");

        when(siteConfigMapper.selectPrimary())
                .thenReturn(current, updated);
        when(siteConfigMapper.updateMusic(any(), eq(OWNER_ID), eq(CURRENT_VERSION)))
                .thenReturn(1);

        var result = ownerSiteService.updateMusic(
                new UpdateMusicDTO(null, null, " ", null, CURRENT_VERSION),
                OWNER_ID
        );

        assertNull(result.config());
        assertEquals(CURRENT_VERSION + 1, result.version());
    }

    @Test
    void updateAppearance_savesOnlyAppearanceBusinessFieldsInJson() {
        when(siteConfigMapper.selectPrimary())
                .thenReturn(config(CURRENT_VERSION), config(CURRENT_VERSION + 1));
        when(siteConfigMapper.updateAppearance(any(), eq(OWNER_ID), eq(CURRENT_VERSION)))
                .thenReturn(1);

        var request = new UpdateAppearanceDTO(
                "immersive",
                "wallpaper",
                0.54,
                0.30,
                new UpdateAppearanceDTO.WallpaperDTO(
                        "/media/desktop.jpg",
                        "/media/mobile.jpg"
                ),
                true,
                CURRENT_VERSION
        );

        ownerSiteService.updateAppearance(request, OWNER_ID);

        ArgumentCaptor<SiteConfig> captor = ArgumentCaptor.forClass(SiteConfig.class);
        verify(siteConfigMapper).updateAppearance(
                captor.capture(),
                eq(OWNER_ID),
                eq(CURRENT_VERSION)
        );

        String json = captor.getValue().getAppearanceJson();
        assertTrue(json.contains("\"layoutMode\":\"immersive\""));
        assertFalse(json.contains("\"version\""));
    }

    @Test
    void updatePageMedia_savesAllPageItemsWithoutSharedVersionInJson() {
        when(siteConfigMapper.selectPrimary())
                .thenReturn(config(CURRENT_VERSION), config(CURRENT_VERSION + 1));
        when(siteConfigMapper.updatePageMedia(any(), eq(OWNER_ID), eq(CURRENT_VERSION)))
                .thenReturn(1);

        UpdatePageMediaDTO.PageMediaItemDTO item =
                new UpdatePageMediaDTO.PageMediaItemDTO(
                        null,
                        "/media/page.jpg",
                        null,
                        "drift",
                        "petals"
                );

        ownerSiteService.updatePageMedia(
                new UpdatePageMediaDTO(
                        item,
                        item,
                        item,
                        item,
                        item,
                        CURRENT_VERSION
                ),
                OWNER_ID
        );

        ArgumentCaptor<SiteConfig> captor = ArgumentCaptor.forClass(SiteConfig.class);
        verify(siteConfigMapper).updatePageMedia(
                captor.capture(),
                eq(OWNER_ID),
                eq(CURRENT_VERSION)
        );

        String json = captor.getValue().getPageMediaJson();
        assertTrue(json.contains("\"moments\""));
        assertTrue(json.contains("\"about\""));
        assertFalse(json.contains("\"version\""));
    }

    private SiteConfig config(Integer version) {
        SiteConfig siteConfig = new SiteConfig();
        siteConfig.setId(1L);
        siteConfig.setSingletonKey("PRIMARY");
        siteConfig.setVersion(version);
        return siteConfig;
    }
}
