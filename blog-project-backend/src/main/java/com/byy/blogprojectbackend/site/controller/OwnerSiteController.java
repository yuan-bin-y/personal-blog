package com.byy.blogprojectbackend.site.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.site.dto.UpdateAnnouncementDTO;
import com.byy.blogprojectbackend.site.dto.UpdateAppearanceDTO;
import com.byy.blogprojectbackend.site.dto.UpdateHeroDTO;
import com.byy.blogprojectbackend.site.dto.UpdateMusicDTO;
import com.byy.blogprojectbackend.site.dto.UpdatePageMediaDTO;
import com.byy.blogprojectbackend.site.dto.UpdateSiteBasicDTO;
import com.byy.blogprojectbackend.site.service.OwnerSiteService;
import com.byy.blogprojectbackend.site.vo.AnnouncementVO;
import com.byy.blogprojectbackend.site.vo.AppearanceVO;
import com.byy.blogprojectbackend.site.vo.HeroVO;
import com.byy.blogprojectbackend.site.vo.MusicVO;
import com.byy.blogprojectbackend.site.vo.PageMediaVO;
import com.byy.blogprojectbackend.site.vo.SiteBasicVO;
import com.byy.blogprojectbackend.site.vo.SiteConfigSectionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Owner 站点配置接口。
 */
@RestController
@RequestMapping("/api/owner/site")
@RequiredArgsConstructor
public class OwnerSiteController {

    private final OwnerSiteService ownerSiteService;

    /**
     * 更新空间基本信息。
     */
    @PutMapping("/basic")
    public Result<SiteConfigSectionVO<SiteBasicVO>> updateBasic(
            @Valid @RequestBody UpdateSiteBasicDTO updateSiteBasicDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                ownerSiteService.updateBasic(
                        updateSiteBasicDTO,
                        getOwnerId(jwt)
                )
        );
    }

    /**
     * 更新 Hero 配置。
     */
    @PutMapping("/hero")
    public Result<SiteConfigSectionVO<HeroVO>> updateHero(
            @Valid @RequestBody UpdateHeroDTO updateHeroDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                ownerSiteService.updateHero(
                        updateHeroDTO,
                        getOwnerId(jwt)
                )
        );
    }

    /**
     * 从已经通过 Spring Security 验证的 JWT 中获取 Owner ID。
     */
    private Long getOwnerId(Jwt jwt) {
        String ownerIdValue = jwt.getClaimAsString(
                JwtTokenService.CLAIM_USER_ID
        );

        return Long.valueOf(ownerIdValue);
    }

    /**
     * 更新空间公告。
     */
    @PutMapping("/announcement")
    public Result<SiteConfigSectionVO<AnnouncementVO>> updateAnnouncement(
            @Valid @RequestBody UpdateAnnouncementDTO updateAnnouncementDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                ownerSiteService.updateAnnouncement(
                        updateAnnouncementDTO,
                        getOwnerId(jwt)
                )
        );
    }

    /**
     * 更新空间音乐。
     */
    @PutMapping("/music")
    public Result<SiteConfigSectionVO<MusicVO>> updateMusic(
            @Valid @RequestBody UpdateMusicDTO updateMusicDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                ownerSiteService.updateMusic(
                        updateMusicDTO,
                        getOwnerId(jwt)
                )
        );
    }

    /**
     * 更新默认空间外观。
     */
    @PutMapping("/appearance")
    public Result<SiteConfigSectionVO<AppearanceVO>> updateAppearance(
            @Valid @RequestBody UpdateAppearanceDTO updateAppearanceDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                ownerSiteService.updateAppearance(
                        updateAppearanceDTO,
                        getOwnerId(jwt)
                )
        );
    }

    /**
     * 更新各页面媒体配置。
     */
    @PutMapping("/page-media")
    public Result<SiteConfigSectionVO<PageMediaVO>> updatePageMedia(
            @Valid @RequestBody UpdatePageMediaDTO updatePageMediaDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                ownerSiteService.updatePageMedia(
                        updatePageMediaDTO,
                        getOwnerId(jwt)
                )
        );
    }
}
