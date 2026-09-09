package com.byy.blogprojectbackend.site.dto;

import com.byy.blogprojectbackend.site.enums.BackgroundMode;
import com.byy.blogprojectbackend.site.enums.LayoutMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Owner 更新默认空间外观的请求参数。
 */
public record UpdateAppearanceDTO(

        @NotBlank(message = "布局模式不能为空")
        @Pattern(
                regexp = LayoutMode.VALIDATION_PATTERN,
                message = LayoutMode.VALIDATION_MESSAGE
        )
        String layoutMode,

        @NotBlank(message = "背景模式不能为空")
        @Pattern(
                regexp = BackgroundMode.VALIDATION_PATTERN,
                message = BackgroundMode.VALIDATION_MESSAGE
        )
        String backgroundMode,

        @NotNull(message = "内容表面透明度不能为空")
        @DecimalMin(value = "0.0", message = "内容表面透明度不能小于0")
        @DecimalMax(value = "1.0", message = "内容表面透明度不能大于1")
        Double surfaceOpacity,

        @NotNull(message = "背景遮罩强度不能为空")
        @DecimalMin(value = "0.0", message = "背景遮罩强度不能小于0")
        @DecimalMax(value = "1.0", message = "背景遮罩强度不能大于1")
        Double backdropShade,

        @NotNull(message = "壁纸配置不能为空")
        @Valid
        WallpaperDTO wallpaper,

        @NotNull(message = "访客控制开关不能为空")
        Boolean allowVisitorControls,

        /** site_config 当前共享版本。 */
        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于0")
        Integer version
) {

    /** 桌面端和移动端壁纸配置。 */
    public record WallpaperDTO(
            String desktop,
            String mobile
    ) {
    }
}
