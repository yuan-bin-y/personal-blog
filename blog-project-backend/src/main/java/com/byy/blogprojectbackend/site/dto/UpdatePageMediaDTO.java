package com.byy.blogprojectbackend.site.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Owner 更新各页面媒体配置的请求参数。
 */
public record UpdatePageMediaDTO(

        @NotNull(message = "说说页面媒体配置不能为空")
        @Valid
        PageMediaItemDTO moments,

        @NotNull(message = "留言板页面媒体配置不能为空")
        @Valid
        PageMediaItemDTO guestbook,

        @NotNull(message = "技术页面媒体配置不能为空")
        @Valid
        PageMediaItemDTO tech,

        @NotNull(message = "归档页面媒体配置不能为空")
        @Valid
        PageMediaItemDTO archive,

        @NotNull(message = "关于页面媒体配置不能为空")
        @Valid
        PageMediaItemDTO about,

        /** site_config 当前共享版本。 */
        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于0")
        Integer version
) {

    /**
     * 单个页面的媒体资源与轻量动效配置。
     *
     * <p>所有字段都允许为 null，表示该页面不使用对应资源或效果。</p>
     */
    public record PageMediaItemDTO(
            String video,
            String poster,
            String overlay,
            String motion,
            String effect
    ) {
    }
}
