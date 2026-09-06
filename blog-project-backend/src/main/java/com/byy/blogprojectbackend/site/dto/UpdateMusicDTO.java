package com.byy.blogprojectbackend.site.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Owner 更新空间音乐的请求参数。
 *
 * <p>audioUrl 为空时表示关闭背景音乐，因此音乐信息允许为 null。</p>
 */
public record UpdateMusicDTO(

        @Size(max = 180, message = "音乐标题不能超过180个字符")
        String title,

        @Size(max = 180, message = "音乐作者不能超过180个字符")
        String artist,

        @Size(max = 1024, message = "音频地址不能超过1024个字符")
        String audioUrl,

        @Size(max = 1024, message = "音乐封面地址不能超过1024个字符")
        String cover,

        /** site_config 当前共享版本。 */
        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于0")
        Integer version
) {
}
