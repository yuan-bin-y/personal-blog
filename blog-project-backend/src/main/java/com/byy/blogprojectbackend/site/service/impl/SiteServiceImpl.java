package com.byy.blogprojectbackend.site.service.impl;

import com.byy.blogprojectbackend.guestbook.mapper.GuestbookMapper;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.enums.PostType;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import com.byy.blogprojectbackend.site.entity.SiteConfig;
import com.byy.blogprojectbackend.site.enums.SiteConfigKey;
import com.byy.blogprojectbackend.site.mapper.SiteConfigMapper;
import com.byy.blogprojectbackend.site.service.SiteService;
import com.byy.blogprojectbackend.site.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 站点公开业务实现。
 */
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteConfigMapper siteConfigMapper;
    private final SpaceProfileMapper spaceProfileMapper;
    private final PostMapper postMapper;
    private final GuestbookMapper guestbookMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取空间初始化数据。
     */
    @Override
    public SiteBootstrapVO getBootstrap() {

        // 1. 获取站点主配置
        SiteConfig config = siteConfigMapper.selectPrimary();

        if (config == null) {
            throw new IllegalStateException(SiteConfigKey.PRIMARY.label() + "不存在，请先执行数据库初始化");
        }

        // 2. 获取站长资料
        SpaceProfile profile = spaceProfileMapper.selectOwnerProfile();

        if (profile == null) {
            throw new IllegalStateException("空间主人资料不存在，请先执行数据库初始化");
        }

        // 3. 获取公开内容统计
        int techPostCount =
                postMapper.countPublishedByType(PostType.TECH.code());

        int momentCount =
                postMapper.countPublishedByType(PostType.MOMENT.code());

        int guestbookCount =
                guestbookMapper.countPublicTopLevel();

        // 4. 组装站点基础信息
        SiteBasicVO basic = buildBasic(config);

        // 5. 组装站长资料
        ProfileVO profileVO = buildProfile(
                profile,
                techPostCount,
                momentCount,
                guestbookCount
        );

        // 6. 组装首页 Hero
        HeroVO hero = buildHero(config);

        // 7. 组装公告
        AnnouncementVO announcement =
                buildAnnouncement(config);

        // 8. 组装音乐
        MusicVO music =
                buildMusic(config);

        // 9. 解析 JSON 配置
        AppearanceVO appearance =
                readJson(
                        config.getAppearanceJson(),
                        AppearanceVO.class
                );

        PageMediaVO pageMedia =
                readJson(
                        config.getPageMediaJson(),
                        PageMediaVO.class
                );

        ExternalLinksVO externalLinks =
                readJson(
                        config.getExternalLinksJson(),
                        ExternalLinksVO.class
                );

        // 10. 组装最终 bootstrap
        return new SiteBootstrapVO(
                basic,
                profileVO,
                hero,
                announcement,
                music,
                appearance,
                pageMedia,
                externalLinks,
                toInstant(config.getUpdatedAt()),
                config.getVersion()
        );
    }

    /**
     * 组装站点基础信息。
     */
    private SiteBasicVO buildBasic(SiteConfig config) {
        return new SiteBasicVO(
                config.getSiteName(),
                config.getSiteChineseName(),
                config.getSiteDescription()
        );
    }

    /**
     * 组装站长资料。
     */
    private ProfileVO buildProfile(
            SpaceProfile profile,
            int techPostCount,
            int momentCount,
            int guestbookCount
    ) {

        ProfileVO.ProfileStatusVO status =
                new ProfileVO.ProfileStatusVO(
                        profile.getStatusLabel(),
                        profile.getStatusText(),
                        profile.getStatusEmoji()
                );

        ProfileVO.ProfileStatsVO stats =
                new ProfileVO.ProfileStatsVO(
                        techPostCount,
                        momentCount,
                        guestbookCount
                );

        return new ProfileVO(
                String.valueOf(profile.getId()),
                String.valueOf(profile.getUserId()),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getRoleText(),
                profile.getBio(),
                status,
                stats,
                profile.getVersion(),
                toInstant(profile.getUpdatedAt())
        );
    }

    /**
     * 组装首页 Hero。
     */
    private HeroVO buildHero(SiteConfig config) {
        return new HeroVO(
                config.getHeroEyebrow(),
                config.getHeroTitle(),
                config.getHeroSubtitle(),
                config.getHeroDesktopVideoUrl(),
                config.getHeroMobileVideoUrl(),
                config.getHeroPosterUrl()
        );
    }

    /**
     * 组装公告。
     *
     * 公告关闭或内容为空时，对外返回 null。
     */
    private AnnouncementVO buildAnnouncement(
            SiteConfig config
    ) {

        String content =
                config.getAnnouncementContent();

        if (!Boolean.TRUE.equals(
                config.getAnnouncementEnabled()
        )) {
            return null;
        }

        if (content == null || content.isBlank()) {
            return null;
        }

        return new AnnouncementVO(
                content,
                true
        );
    }

    /**
     * 组装背景音乐。
     *
     * audioUrl 为空时，对外返回 null。
     */
    private MusicVO buildMusic(SiteConfig config) {

        String audioUrl =
                config.getMusicAudioUrl();

        if (audioUrl == null || audioUrl.isBlank()) {
            return null;
        }

        return new MusicVO(
                config.getMusicTitle(),
                config.getMusicArtist(),
                audioUrl,
                config.getMusicCoverUrl()
        );
    }

    /**
     * 把数据库中的 JSON 字符串解析成具体 VO。
     */
    private <T> T readJson(
            String json,
            Class<T> targetType
    ) {

        if (json == null || json.isBlank()) {
            throw new IllegalStateException(
                    "站点 JSON 配置尚未初始化：" + targetType.getSimpleName()
            );
        }

        try {
            return objectMapper.readValue(
                    json,
                    targetType
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "站点 JSON 配置格式不正确：" + targetType.getSimpleName(),
                    e
            );
        }
    }

    /**
     * 数据库 DATETIME 转 API 的 UTC Instant。
     */
    private Instant toInstant(LocalDateTime time) {

        if (time == null) {
            return null;
        }

        return time.toInstant(
                ZoneOffset.UTC
        );
    }
}
