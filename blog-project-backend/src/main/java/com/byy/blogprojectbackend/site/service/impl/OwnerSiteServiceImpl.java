package com.byy.blogprojectbackend.site.service.impl;

import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.site.dto.UpdateAnnouncementDTO;
import com.byy.blogprojectbackend.site.dto.UpdateAppearanceDTO;
import com.byy.blogprojectbackend.site.dto.UpdateHeroDTO;
import com.byy.blogprojectbackend.site.dto.UpdateMusicDTO;
import com.byy.blogprojectbackend.site.dto.UpdatePageMediaDTO;
import com.byy.blogprojectbackend.site.dto.UpdateSiteBasicDTO;
import com.byy.blogprojectbackend.site.entity.SiteConfig;
import com.byy.blogprojectbackend.site.mapper.SiteConfigMapper;
import com.byy.blogprojectbackend.site.service.OwnerSiteService;
import com.byy.blogprojectbackend.site.vo.AnnouncementVO;
import com.byy.blogprojectbackend.site.vo.AppearanceVO;
import com.byy.blogprojectbackend.site.vo.HeroVO;
import com.byy.blogprojectbackend.site.vo.MusicVO;
import com.byy.blogprojectbackend.site.vo.PageMediaVO;
import com.byy.blogprojectbackend.site.vo.SiteBasicVO;
import com.byy.blogprojectbackend.site.vo.SiteConfigSectionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Owner 站点配置业务实现。
 */
@Service
@RequiredArgsConstructor
public class OwnerSiteServiceImpl implements OwnerSiteService {

    private final SiteConfigMapper siteConfigMapper;
    private final ObjectMapper objectMapper;

    /**
     * 更新空间基本信息。
     */
    @Override
    @Transactional
    public SiteConfigSectionVO<SiteBasicVO> updateBasic(
            UpdateSiteBasicDTO updateSiteBasicDTO,
            Long ownerId
    ) {
        SiteConfig siteConfig = getPrimaryConfig();

        siteConfig.setSiteName(
                updateSiteBasicDTO.name()
        );

        siteConfig.setSiteChineseName(
                updateSiteBasicDTO.chineseName()
        );

        siteConfig.setSiteDescription(
                updateSiteBasicDTO.description()
        );

        int affectedRows = siteConfigMapper.updateSiteBasic(
                siteConfig,
                ownerId,
                updateSiteBasicDTO.version()
        );

        checkVersionConflict(affectedRows);

        SiteConfig updatedConfig = getPrimaryConfig();

        SiteBasicVO basic = new SiteBasicVO(
                updatedConfig.getSiteName(),
                updatedConfig.getSiteChineseName(),
                updatedConfig.getSiteDescription()
        );

        return new SiteConfigSectionVO<>(
                basic,
                updatedConfig.getVersion(),
                toInstant(updatedConfig.getUpdatedAt())
        );
    }

    /**
     * 更新 Hero 配置。
     */
    @Override
    @Transactional
    public SiteConfigSectionVO<HeroVO> updateHero(
            UpdateHeroDTO updateHeroDTO,
            Long ownerId
    ) {
        /*
         * 1. 查询 PRIMARY 配置。
         *
         * 需要获得数据库记录 ID，
         * 同时确认系统配置已经完成初始化。
         */
        SiteConfig siteConfig = getPrimaryConfig();

        /*
         * 2. 只将 Hero DTO 允许修改的字段写入 Entity。
         *
         * Basic、公告、音乐、外观等其他配置保持不变。
         */
        siteConfig.setHeroEyebrow(
                updateHeroDTO.eyebrow()
        );

        siteConfig.setHeroTitle(
                updateHeroDTO.heroTitle()
        );

        siteConfig.setHeroSubtitle(
                updateHeroDTO.heroSubtitle()
        );

        siteConfig.setHeroDesktopVideoUrl(
                updateHeroDTO.desktopVideo()
        );

        siteConfig.setHeroMobileVideoUrl(
                updateHeroDTO.mobileVideo()
        );

        siteConfig.setHeroPosterUrl(
                updateHeroDTO.poster()
        );

        /*
         * 3. 按 id + 共享 version 更新。
         *
         * ownerId 写入 updated_by；
         * DTO 中的 version 只作为旧版本条件。
         */
        int affectedRows = siteConfigMapper.updateHero(
                siteConfig,
                ownerId,
                updateHeroDTO.version()
        );

        /*
         * 4. 更新不到记录表示共享 version 已经过期。
         */
        checkVersionConflict(affectedRows);

        /*
         * 5. 重新查询数据库。
         *
         * 需要获得数据库更新后的：
         * - Hero 字段
         * - version
         * - updatedAt
         */
        SiteConfig updatedConfig = getPrimaryConfig();

        /*
         * 6. Entity 转换为 HeroVO。
         *
         * 数据库字段使用 Url 后缀，
         * API 字段使用更简洁的名称。
         */
        HeroVO hero = new HeroVO(
                updatedConfig.getHeroEyebrow(),
                updatedConfig.getHeroTitle(),
                updatedConfig.getHeroSubtitle(),
                updatedConfig.getHeroDesktopVideoUrl(),
                updatedConfig.getHeroMobileVideoUrl(),
                updatedConfig.getHeroPosterUrl()
        );

        /*
         * 7. 返回 Hero 和最新共享版本。
         */
        return new SiteConfigSectionVO<>(
                hero,
                updatedConfig.getVersion(),
                toInstant(updatedConfig.getUpdatedAt())
        );
    }

    /**
     * 更新空间公告。
     */
    @Override
    @Transactional
    public SiteConfigSectionVO<AnnouncementVO> updateAnnouncement(
            UpdateAnnouncementDTO updateAnnouncementDTO,
            Long ownerId
    ) {
        SiteConfig siteConfig = getPrimaryConfig();

        // 只修改公告相关字段，不影响同一行中的其他站点配置。
        siteConfig.setAnnouncementContent(updateAnnouncementDTO.content());
        siteConfig.setAnnouncementEnabled(updateAnnouncementDTO.enabled());

        int affectedRows = siteConfigMapper.updateAnnouncement(
                siteConfig,
                ownerId,
                updateAnnouncementDTO.version()
        );

        checkVersionConflict(affectedRows);

        // 重新读取数据库生成的新 version 和 updatedAt。
        SiteConfig updatedConfig = getPrimaryConfig();
        AnnouncementVO announcement = null;

        // 与公开 Bootstrap 保持一致：关闭或空公告对外表示为 null。
        if (Boolean.TRUE.equals(updatedConfig.getAnnouncementEnabled())) {
            String content = updatedConfig.getAnnouncementContent();

            if (content != null && !content.isBlank()) {
                announcement = new AnnouncementVO(content, true);
            }
        }

        return new SiteConfigSectionVO<>(
                announcement,
                updatedConfig.getVersion(),
                toInstant(updatedConfig.getUpdatedAt())
        );
    }

    /**
     * 更新空间音乐。
     */
    @Override
    @Transactional
    public SiteConfigSectionVO<MusicVO> updateMusic(
            UpdateMusicDTO updateMusicDTO,
            Long ownerId
    ) {
        SiteConfig siteConfig = getPrimaryConfig();

        // 音乐使用独立列保存，只修改音乐相关字段。
        siteConfig.setMusicTitle(updateMusicDTO.title());
        siteConfig.setMusicArtist(updateMusicDTO.artist());
        siteConfig.setMusicAudioUrl(updateMusicDTO.audioUrl());
        siteConfig.setMusicCoverUrl(updateMusicDTO.cover());

        int affectedRows = siteConfigMapper.updateMusic(
                siteConfig,
                ownerId,
                updateMusicDTO.version()
        );

        checkVersionConflict(affectedRows);

        SiteConfig updatedConfig = getPrimaryConfig();

        return new SiteConfigSectionVO<>(
                buildMusic(updatedConfig),
                updatedConfig.getVersion(),
                toInstant(updatedConfig.getUpdatedAt())
        );
    }

    /**
     * 更新默认空间外观。
     */
    @Override
    @Transactional
    public SiteConfigSectionVO<AppearanceVO> updateAppearance(
            UpdateAppearanceDTO updateAppearanceDTO,
            Long ownerId
    ) {
        SiteConfig siteConfig = getPrimaryConfig();

        /*
         * DTO 中包含乐观锁 version，不能直接把整个 DTO 写入 JSON。
         * 先转换为只包含业务配置的 AppearanceVO，再序列化保存。
         */
        AppearanceVO appearance = new AppearanceVO(
                updateAppearanceDTO.layoutMode(),
                updateAppearanceDTO.backgroundMode(),
                updateAppearanceDTO.surfaceOpacity(),
                updateAppearanceDTO.backdropShade(),
                new AppearanceVO.WallpaperVO(
                        updateAppearanceDTO.wallpaper().desktop(),
                        updateAppearanceDTO.wallpaper().mobile()
                ),
                updateAppearanceDTO.allowVisitorControls()
        );

        siteConfig.setAppearanceJson(writeJson(appearance));

        int affectedRows = siteConfigMapper.updateAppearance(
                siteConfig,
                ownerId,
                updateAppearanceDTO.version()
        );

        checkVersionConflict(affectedRows);

        SiteConfig updatedConfig = getPrimaryConfig();

        return new SiteConfigSectionVO<>(
                appearance,
                updatedConfig.getVersion(),
                toInstant(updatedConfig.getUpdatedAt())
        );
    }

    /**
     * 更新各页面媒体配置。
     */
    @Override
    @Transactional
    public SiteConfigSectionVO<PageMediaVO> updatePageMedia(
            UpdatePageMediaDTO updatePageMediaDTO,
            Long ownerId
    ) {
        SiteConfig siteConfig = getPrimaryConfig();

        /*
         * 页面媒体配置整体存放在 page_media_json 中。
         * 这里显式转换每个页面，防止请求专用字段 version 被保存进 JSON。
         */
        PageMediaVO pageMedia = new PageMediaVO(
                toPageMediaItem(updatePageMediaDTO.moments()),
                toPageMediaItem(updatePageMediaDTO.guestbook()),
                toPageMediaItem(updatePageMediaDTO.tech()),
                toPageMediaItem(updatePageMediaDTO.archive()),
                toPageMediaItem(updatePageMediaDTO.about())
        );

        siteConfig.setPageMediaJson(writeJson(pageMedia));

        int affectedRows = siteConfigMapper.updatePageMedia(
                siteConfig,
                ownerId,
                updatePageMediaDTO.version()
        );

        checkVersionConflict(affectedRows);

        SiteConfig updatedConfig = getPrimaryConfig();

        return new SiteConfigSectionVO<>(
                pageMedia,
                updatedConfig.getVersion(),
                toInstant(updatedConfig.getUpdatedAt())
        );
    }

    /**
     * 音频地址为空表示没有启用背景音乐，与公开 Bootstrap 的返回规则一致。
     */
    private MusicVO buildMusic(SiteConfig siteConfig) {
        String audioUrl = siteConfig.getMusicAudioUrl();

        if (audioUrl == null || audioUrl.isBlank()) {
            return null;
        }

        return new MusicVO(
                siteConfig.getMusicTitle(),
                siteConfig.getMusicArtist(),
                audioUrl,
                siteConfig.getMusicCoverUrl()
        );
    }

    /**
     * 将页面媒体 DTO 转为对外统一的 VO。
     */
    private PageMediaVO.PageMediaItemVO toPageMediaItem(
            UpdatePageMediaDTO.PageMediaItemDTO item
    ) {
        return new PageMediaVO.PageMediaItemVO(
                item.video(),
                item.poster(),
                item.overlay(),
                item.motion(),
                item.effect()
        );
    }

    /**
     * 将结构化配置序列化为数据库 JSON 字符串。
     */
    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to serialize site JSON config",
                    exception
            );
        }
    }

    /**
     * 查询系统唯一的 PRIMARY 配置。
     */
    private SiteConfig getPrimaryConfig() {
        SiteConfig siteConfig =
                siteConfigMapper.selectPrimary();

        if (siteConfig == null) {
            throw new IllegalStateException(
                    "PRIMARY site config does not exist"
            );
        }

        return siteConfig;
    }

    /**
     * 检查乐观锁更新结果。
     */
    private void checkVersionConflict(int affectedRows) {
        if (affectedRows == 0) {
            throw new VersionConflictException(
                    "Site config version conflict"
            );
        }
    }

    /**
     * 数据库 LocalDateTime 转换为 API UTC 时间。
     */
    private Instant toInstant(LocalDateTime time) {
        if (time == null) {
            return null;
        }

        return time.toInstant(ZoneOffset.UTC);
    }
}
