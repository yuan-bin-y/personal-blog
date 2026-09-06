package com.byy.blogprojectbackend.site.vo;

import java.time.Instant;

/**
 * Owner 更新某一块站点配置后的统一响应。
 *
 * @param config    更新后的具体配置
 * @param version   site_config 最新共享版本
 * @param updatedAt 最后更新时间
 */
public record SiteConfigSectionVO<T>(

        T config,

        Integer version,

        Instant updatedAt

) {
}