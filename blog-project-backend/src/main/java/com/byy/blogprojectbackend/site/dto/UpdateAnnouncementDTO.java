package com.byy.blogprojectbackend.site.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Owner 更新空间公告的请求参数。
 */
public record UpdateAnnouncementDTO(

        /** 公告内容；关闭公告时允许为空。 */
        @Size(max = 2000, message = "公告内容不能超过2000个字符")
        String content,

        /** 是否启用公告。 */
        @NotNull(message = "公告启用状态不能为空")
        Boolean enabled,

        /** site_config 当前共享版本。 */
        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于0")
        Integer version
) {

    /** 启用公告时，公告内容不能为空。 */
    @AssertTrue(message = "启用公告时公告内容不能为空")
    public boolean isContentValid() {
        return !Boolean.TRUE.equals(enabled)
                || (content != null && !content.isBlank());
    }
}
