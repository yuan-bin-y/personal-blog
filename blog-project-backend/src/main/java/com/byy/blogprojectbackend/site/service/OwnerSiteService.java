package com.byy.blogprojectbackend.site.service;

import com.byy.blogprojectbackend.site.dto.UpdateAnnouncementDTO;
import com.byy.blogprojectbackend.site.dto.UpdateAppearanceDTO;
import com.byy.blogprojectbackend.site.dto.UpdateHeroDTO;
import com.byy.blogprojectbackend.site.dto.UpdateMusicDTO;
import com.byy.blogprojectbackend.site.dto.UpdatePageMediaDTO;
import com.byy.blogprojectbackend.site.dto.UpdateSiteBasicDTO;
import com.byy.blogprojectbackend.site.vo.AnnouncementVO;
import com.byy.blogprojectbackend.site.vo.AppearanceVO;
import com.byy.blogprojectbackend.site.vo.HeroVO;
import com.byy.blogprojectbackend.site.vo.MusicVO;
import com.byy.blogprojectbackend.site.vo.PageMediaVO;
import com.byy.blogprojectbackend.site.vo.SiteBasicVO;
import com.byy.blogprojectbackend.site.vo.SiteConfigSectionVO;

/**
 * Owner 站点配置业务接口。
 */
public interface OwnerSiteService {

    /**
     * 更新空间基本信息。
     */
    SiteConfigSectionVO<SiteBasicVO> updateBasic(
            UpdateSiteBasicDTO updateSiteBasicDTO,
            Long ownerId
    );

    /**
     * 更新 Hero 配置。
     */
    SiteConfigSectionVO<HeroVO> updateHero(
            UpdateHeroDTO updateHeroDTO,
            Long ownerId
    );

    /**
     * 更新空间公告。
     */
    SiteConfigSectionVO<AnnouncementVO> updateAnnouncement(
            UpdateAnnouncementDTO updateAnnouncementDTO,
            Long ownerId
    );

    /**
     * 更新空间音乐。
     */
    SiteConfigSectionVO<MusicVO> updateMusic(
            UpdateMusicDTO updateMusicDTO,
            Long ownerId
    );

    /**
     * 更新默认空间外观。
     */
    SiteConfigSectionVO<AppearanceVO> updateAppearance(
            UpdateAppearanceDTO updateAppearanceDTO,
            Long ownerId
    );

    /**
     * 更新各页面媒体配置。
     */
    SiteConfigSectionVO<PageMediaVO> updatePageMedia(
            UpdatePageMediaDTO updatePageMediaDTO,
            Long ownerId
    );
}
