package com.byy.blogprojectbackend.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Owner 更新空间基本信息的请求参数。
 */
public record UpdateSiteBasicDTO(

        /**
         * 英文站点名称，例如 BinSpace。
         */
        @NotBlank(message = "站点名称不能为空")
        @Size(max = 64, message = "站点名称不能超过64个字符")
        String name,

        /**
         * 中文站点名称，例如玢的空间。
         */
        @NotBlank(message = "中文站点名称不能为空")
        @Size(max = 64, message = "中文站点名称不能超过64个字符")
        String chineseName,

        /**
         * 空间简介，允许为空。
         */
        @Size(max = 255, message = "空间简介不能超过255个字符")
        String description,

        /**
         * site_config 当前共享乐观锁版本。
         */
        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于0")
        Integer version

) {
}