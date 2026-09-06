package com.byy.blogprojectbackend.site.controller;

import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.site.service.SiteService;
import com.byy.blogprojectbackend.site.vo.SiteBootstrapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开站点接口。
 */
@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;


    /**
     * 获取空间初始化数据。
     */
    @GetMapping("/bootstrap")
    public Result<SiteBootstrapVO> getBootstrap() {
        return Result.success(
                siteService.getBootstrap()
        );
    }
}