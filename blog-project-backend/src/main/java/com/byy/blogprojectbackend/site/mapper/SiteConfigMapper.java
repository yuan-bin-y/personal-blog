package com.byy.blogprojectbackend.site.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.site.entity.SiteConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * site_config 表数据库操作。
 */
@Mapper
public interface SiteConfigMapper
        extends BaseMapper<SiteConfig> {

    /**
     * 查询全站唯一的 PRIMARY 配置。
     */
    SiteConfig selectPrimary();

    /**
     * 使用乐观锁更新空间基本信息。
     */
    int updateSiteBasic(
            @Param("siteConfig") SiteConfig siteConfig,
            @Param("ownerId") Long ownerId,
            @Param("version") Integer version
    );

    /**
     * 使用乐观锁更新 Hero 配置。
     */
    int updateHero(
            @Param("siteConfig") SiteConfig siteConfig,
            @Param("ownerId") Long ownerId,
            @Param("version") Integer version
    );
    /**
     * 使用乐观锁更新空间公告。
     */
    int updateAnnouncement(
            @Param("siteConfig") SiteConfig siteConfig,
            @Param("ownerId") Long ownerId,
            @Param("version") Integer version
    );

    /**
     * 使用乐观锁更新空间音乐。
     */
    int updateMusic(
            @Param("siteConfig") SiteConfig siteConfig,
            @Param("ownerId") Long ownerId,
            @Param("version") Integer version
    );

    /**
     * 使用乐观锁更新默认空间外观 JSON。
     */
    int updateAppearance(
            @Param("siteConfig") SiteConfig siteConfig,
            @Param("ownerId") Long ownerId,
            @Param("version") Integer version
    );

    /**
     * 使用乐观锁更新页面媒体配置 JSON。
     */
    int updatePageMedia(
            @Param("siteConfig") SiteConfig siteConfig,
            @Param("ownerId") Long ownerId,
            @Param("version") Integer version
    );
}
