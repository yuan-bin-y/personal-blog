package com.byy.blogprojectbackend.common.constant;

import com.byy.blogprojectbackend.user.enums.UserRole;

import java.util.List;

/**
 * BinSpace 的角色与权限定义。
 *
 * <p>每个权限同时包含：
 * 1. 程序内部使用的权限编码
 * 2. 前端展示使用的中文名称
 * </p>
 */
public final class AuthorityConstants {

    private AuthorityConstants() {
    }

    /** 数据库和 API 使用的不带前缀角色编码。 */
    public static final String ROLE_CODE_OWNER = UserRole.OWNER.code();
    public static final String ROLE_CODE_VISITOR = UserRole.VISITOR.code();

    /** Spring Security GrantedAuthority 使用的带 ROLE_ 前缀角色。 */
    public static final String SPRING_ROLE_PREFIX = "ROLE_";
    public static final String ROLE_OWNER = SPRING_ROLE_PREFIX + ROLE_CODE_OWNER;
    public static final String ROLE_VISITOR = SPRING_ROLE_PREFIX + ROLE_CODE_VISITOR;

    /**
     * 权限定义。
     */
    public enum Permission {

        POST_CREATE("POST_CREATE", "发布文章"),
        POST_EDIT("POST_EDIT", "编辑文章"),
        POST_DELETE("POST_DELETE", "删除文章"),

        CATEGORY_MANAGE("CATEGORY_MANAGE", "分类管理"),
        TAG_MANAGE("TAG_MANAGE", "标签管理"),

        PROFILE_EDIT("PROFILE_EDIT", "编辑个人资料"),
        SITE_EDIT("SITE_EDIT", "编辑站点设置"),

        COMMENT_REPLY("COMMENT_REPLY", "回复评论"),
        COMMENT_DELETE("COMMENT_DELETE", "删除评论"),

        GUESTBOOK_REPLY("GUESTBOOK_REPLY", "回复留言"),
        GUESTBOOK_DELETE("GUESTBOOK_DELETE", "删除留言"),

        MEDIA_MANAGE("MEDIA_MANAGE", "媒体管理"),

        SEARCH_REINDEX("SEARCH_REINDEX", "重建搜索索引");

        /** Spring Security 使用的权限编码。 */
        private final String code;

        /** 给前端或用户展示的中文名称。 */
        private final String label;

        Permission(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }

    /** V1 单一 OWNER 拥有的全部业务权限。 */
    public static final List<String> OWNER_PERMISSIONS = List.of(
            Permission.POST_CREATE.getCode(),
            Permission.POST_EDIT.getCode(),
            Permission.POST_DELETE.getCode(),

            Permission.CATEGORY_MANAGE.getCode(),
            Permission.TAG_MANAGE.getCode(),

            Permission.PROFILE_EDIT.getCode(),
            Permission.SITE_EDIT.getCode(),

            Permission.COMMENT_REPLY.getCode(),
            Permission.COMMENT_DELETE.getCode(),

            Permission.GUESTBOOK_REPLY.getCode(),
            Permission.GUESTBOOK_DELETE.getCode(),

            Permission.MEDIA_MANAGE.getCode(),

            Permission.SEARCH_REINDEX.getCode()
    );
}
