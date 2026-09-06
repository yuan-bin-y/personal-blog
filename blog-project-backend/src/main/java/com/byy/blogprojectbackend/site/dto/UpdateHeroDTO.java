package com.byy.blogprojectbackend.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Owner 更新 Hero 配置的请求参数。
 */
public record UpdateHeroDTO(

        /**
         * Hero 顶部英文小标题，允许为空。
         */
        @Size(max = 128, message = "Hero 小标题不能超过128个字符")
        String eyebrow,

        /**
         * Hero 主标题。
         */
        @NotBlank(message = "Hero 主标题不能为空")
        @Size(max = 180, message = "Hero 主标题不能超过180个字符")
        String heroTitle,

        /**
         * Hero 副标题，允许为空。
         */
        @Size(max = 500, message = "Hero 副标题不能超过500个字符")
        String heroSubtitle,

        /**
         * 桌面端视频地址，允许为空。
         */
        @Size(max = 1024, message = "桌面端视频地址不能超过1024个字符")
        String desktopVideo,

        /**
         * 移动端视频地址，允许为空。
         */
        @Size(max = 1024, message = "移动端视频地址不能超过1024个字符")
        String mobileVideo,

        /**
         * Hero 封面地址，允许为空。
         */
        @Size(max = 1024, message = "Hero 封面地址不能超过1024个字符")
        String poster,

        /**
         * site_config 当前共享版本。
         */
        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于0")
        Integer version

) {
}