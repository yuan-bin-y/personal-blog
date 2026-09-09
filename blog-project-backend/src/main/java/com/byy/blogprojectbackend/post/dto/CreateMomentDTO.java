package com.byy.blogprojectbackend.post.dto;

import com.byy.blogprojectbackend.post.enums.PostStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateMomentDTO(
        @NotBlank(message = "说说内容不能为空")
        @Size(max = 2000, message = "说说内容不能超过 2000 个字符") String content,

        @NotNull(message = "说说图片列表不能为空")
        @Size(max = 9, message = "一条说说最多包含 9 张图片") List<@Valid MediaInputDTO> images,

        @NotNull(message = "发布状态不能为空")
        @Pattern(regexp = PostStatus.EDITABLE_PATTERN, message = PostStatus.EDITABLE_MESSAGE)
        String status
) {
}
