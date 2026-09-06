package com.byy.blogprojectbackend.site.service;

import com.byy.blogprojectbackend.site.vo.SiteBootstrapVO;

/**
 * 站点公开业务。
 */
public interface SiteService {

    /**
     * 获取空间初始化数据。
     */
    SiteBootstrapVO getBootstrap();
}