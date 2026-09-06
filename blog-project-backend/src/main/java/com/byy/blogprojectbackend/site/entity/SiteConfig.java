package com.byy.blogprojectbackend.site.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * site_config 表实体。
 *
 * 全站唯一配置记录，singletonKey 固定为 PRIMARY。
 *
 * 包含：
 * 1. 站点基础信息
 * 2. Hero
 * 3. Announcement
 * 4. Music
 * 5. Appearance JSON
 * 6. PageMedia JSON
 * 7. ExternalLinks JSON
 */
@Data
@TableName("site_config")
public class SiteConfig {

    @TableId
    private Long id;

    /**
     * 单例标识，V1 固定为 PRIMARY。
     */
    private String singletonKey;

    // ==================== Basic ====================

    /**
     * 英文站点名称，例如 BinSpace。
     */
    private String siteName;

    /**
     * 中文站点名称。
     */
    private String siteChineseName;

    /**
     * 站点一句话介绍。
     */
    private String siteDescription;


    // ==================== Hero ====================

    /**
     * Hero 顶部小标题。
     */
    private String heroEyebrow;

    /**
     * Hero 主标题。
     */
    private String heroTitle;

    /**
     * Hero 副标题。
     */
    private String heroSubtitle;

    /**
     * 桌面端 Hero 视频地址。
     */
    private String heroDesktopVideoUrl;

    /**
     * 移动端 Hero 视频地址。
     */
    private String heroMobileVideoUrl;

    /**
     * Hero 视频封面地址。
     */
    private String heroPosterUrl;


    // ==================== Announcement ====================

    /**
     * 公告正文。
     */
    private String announcementContent;

    /**
     * 是否展示公告。
     */
    private Boolean announcementEnabled;


    // ==================== Music ====================

    /**
     * 音乐标题。
     */
    private String musicTitle;

    /**
     * 音乐作者。
     */
    private String musicArtist;

    /**
     * 音频地址。
     */
    private String musicAudioUrl;

    /**
     * 音乐封面地址。
     */
    private String musicCoverUrl;


    // ==================== JSON Config ====================

    /**
     * 外观配置 JSON。
     *
     * V1 先作为 String 保存，
     * 后续在 Service 层解析为明确的 VO。
     */
    private String appearanceJson;

    /**
     * 各页面媒体配置 JSON。
     */
    private String pageMediaJson;

    /**
     * 外部链接配置 JSON。
     */
    private String externalLinksJson;


    // ==================== Audit ====================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 最后修改配置的 OWNER ID。
     * 数据库初始化产生的配置允许为 null。
     */
    private Long updatedBy;

    /**
     * Basic、Hero、Announcement、Music、
     * Appearance、PageMedia、ExternalLinks
     * 共用同一个乐观锁版本。
     */
    @Version
    private Integer version;
}