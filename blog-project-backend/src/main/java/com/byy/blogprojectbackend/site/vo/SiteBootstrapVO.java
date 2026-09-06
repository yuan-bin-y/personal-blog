package com.byy.blogprojectbackend.site.vo;

import java.time.Instant;

/**
 * 空间初始化接口的总返回对象。
 *
 * 对应：
 * GET /api/site/bootstrap
 *
 * 负责把站点基础信息、个人资料、Hero、公告、音乐、
 * 外观、页面媒体、外部链接等数据统一组装返回给前端。
 */
public record SiteBootstrapVO(

        /**
         * 站点基础信息。
         */
        SiteBasicVO basic,

        /**
         * OWNER 对外展示的个人资料。
         */
        ProfileVO profile,

        /**
         * 首页 Hero 配置。
         */
        HeroVO hero,

        /**
         * 公告配置。
         * 未启用公告时可以为 null。
         */
        AnnouncementVO announcement,

        /**
         * 音乐配置。
         * 未配置音乐时可以为 null。
         */
        MusicVO music,

        /**
         * 全站外观配置。
         */
        AppearanceVO appearance,

        /**
         * Moments、Tech、Guestbook、Archive、About
         * 等页面的媒体背景配置。
         */
        PageMediaVO pageMedia,

        /**
         * 外部链接。
         */
        ExternalLinksVO externalLinks,

        /**
         * SiteConfig 最后修改时间。
         */
        Instant updatedAt,

        /**
         * site_config 当前共享版本号。
         */
        Integer version

) {
}