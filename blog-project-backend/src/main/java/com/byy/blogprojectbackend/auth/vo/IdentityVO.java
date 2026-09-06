package com.byy.blogprojectbackend.auth.vo;

import java.util.List;

/** 当前访问者身份；匿名访问者也使用该结构返回 VISITOR。 */
public record IdentityVO(
        boolean authenticated,
        String role,
        IdentityUserVO user,
        List<String> permissions
) {

    /** 创建固定的匿名 Visitor 身份。 */
    public static IdentityVO visitor() {
        return new IdentityVO(
                false,
                "VISITOR",
                null,
                List.of()
        );
    }
}
