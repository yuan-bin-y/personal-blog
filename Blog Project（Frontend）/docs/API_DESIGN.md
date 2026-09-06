# BinSpace API 设计（当前基线 + 下一阶段 2～8）

> 版本：V2 Draft 1（用于后续 2～8 功能开发）  
> 数据库：MySQL 8.x `binspace`  
> 后端预期：Java + Spring Boot + Spring Security + MyBatis / MyBatis-Plus  
> 面向前端：当前 Vue Visitor 页面与 Owner UI Mode  
> 本文只定义接口契约，不包含 Controller、Service、Mapper、Entity、SQL 或任何 Java 实现。

## 1. 范围与身份

系统始终只保留两种业务身份：

- `VISITOR`：既可以匿名浏览，也可以注册登录。登录后的 Visitor 可以维护自己的评论、留言与点赞。
- `OWNER`：登录后仍使用与 Visitor 相同的页面和 Layout，但可发布、编辑、删除内容及修改空间配置。

当前后端已经实现 42 个基线接口；第 16～22 章定义下一阶段需要实现的功能 2～8：Visitor 账号与互动、点赞、媒体上传、实时通知、Redis 业务缓存、搜索/推荐、内容高级能力。OpenAPI 共定义 76 个 HTTP 操作，其中 42 个为当前基线、34 个为下一阶段新增契约。新增接口在实现前不代表代码已经存在。

本阶段仍不设计多 OWNER、多空间/多租户、后台 Dashboard、Visitor 发文章、私信和社交关注关系。

## 2. 通用协议

### 2.1 基础约定

- API 基础路径：`/api`；JSON 使用 `camelCase`，数据库使用 `snake_case`。
- JSON 媒体类型为 `application/json; charset=UTF-8`，`204` 响应除外。
- 时间统一返回 UTC ISO-8601，例如 `2026-09-02T10:20:30.123Z`。
- Visitor 内容按 `publishedAt DESC, id DESC`；Owner 内容按 `updatedAt DESC, id DESC` 稳定排序。
- URL 只允许受控站内路径或 `https`；拒绝 `javascript:` 等危险协议。
- TECH Markdown 渲染前必须进行 XSS 清洗。
- API 不直接暴露数据库 Entity，只接收 DTO、返回 VO。

### 2.2 BIGINT JSON 规则

所有数据库 `BIGINT` 主键和外键在请求、响应和路径变量中统一使用十进制 `string`：

```text
id, userId, postId, parentId, categoryId, tagId, mediaId ... : string
```

前端不得转换成 JavaScript `Number`；后端必须统一配置序列化，不能只转换部分 VO。

### 2.3 成功与分页

除 `204 No Content` 外统一包装：

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "traceId": "01J..."
}
```

创建用 201，读取/更新用 200，删除/退出用 204 且不返回 body。

所有标准列表使用 `page`（默认 1，最小 1）和 `pageSize`（默认 10，最小 1，最大 50）：

```text
PageVO<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
  totalPages: number
  hasMore: boolean
}
```

非法分页参数返回 `400 BAD_REQUEST`。Archive 使用带同样分页元数据的 `ArchiveVO`。

### 2.4 错误响应、Status 与 Error Code

```json
{
  "code": "BAD_REQUEST",
  "message": "请求参数不合法",
  "fieldErrors": { "title": "标题不能为空" },
  "data": null,
  "traceId": "01J..."
}
```

| HTTP | Error Code | 含义 |
|---:|---|---|
| 400 | `BAD_REQUEST` | JSON、Query、Path Variable、枚举或 Bean Validation 不合法 |
| 401 | `AUTH_REQUIRED` | 未登录、JWT 失效或 Redis Token Session 已撤销 |
| 401 | `AUTH_FAILED` | Owner 用户名或密码错误，不泄露账号是否存在 |
| 403 | `FORBIDDEN` | 身份或权限不足 |
| 404 | `RESOURCE_NOT_FOUND` | 不存在、已删除，或 Visitor 无权看到的草稿 |
| 409 | `VERSION_CONFLICT` | 乐观锁冲突 |
| 409 | `RESOURCE_CONFLICT` | 唯一键、重复回复或资源状态冲突 |
| 500 | `INTERNAL_ERROR` | 未处理错误，不返回堆栈 |

Bean Validation 失败统一使用 400。版本冲突 `data` 可包含 `currentVersion`，但不能返回 Entity。

## 3. 认证：Spring Security + JWT + Redis

当前已落地方案为 **Spring Security + Bearer JWT + Redis Token Session**，不再使用 Server Session 与 CSRF Token。

- 登录成功返回 Access Token，并以 `jti` 为键在 Redis 登记有效会话。
- 受保护请求发送 `Authorization: Bearer <accessToken>`。
- 退出登录时撤销 Redis 中对应 Token；JWT 即使未过期也不能继续使用。
- JWT Secret、MySQL/Redis 密码等只从本地配置或环境变量读取，不进入 Git。
- CORS 只允许明确的前端 Origin。Bearer Token 不依赖 Cookie，写请求不要求 CSRF Header。
- V2 的 Visitor 和 Owner 共用认证链路，通过数据库 role、Spring Security Authority 和接口权限区分。

```text
LoginDTO {
  username: string  // 1..64
  password: string  // 1..128，仅传输，不写日志
}

LoginVO {
  accessToken: string
  tokenType: "Bearer"
  expiresIn: number
  identity: IdentityVO
}

IdentityVO {
  authenticated: boolean
  role: "VISITOR" | "OWNER"
  user: null | {
    id: string
    username: string
    displayName: string
    avatar: string | null
  }
  permissions: string[]
}
```

OWNER permissions：`POST_CREATE`、`POST_EDIT`、`POST_DELETE`、`CATEGORY_MANAGE`、`TAG_MANAGE`、`PROFILE_EDIT`、`SITE_EDIT`、`COMMENT_REPLY`、`COMMENT_DELETE`、`GUESTBOOK_REPLY`、`GUESTBOOK_DELETE`、`MEDIA_MANAGE`、`SEARCH_REINDEX`。

### 3.1 Auth 接口

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/auth/login` | Public | 无 | `LoginDTO` | `LoginVO` | 200, 400, 401, 500 | `BAD_REQUEST`, `AUTH_FAILED`, `INTERNAL_ERROR` | Visitor/Owner 登录；当前代码已支持 Owner，V2 扩展 Visitor |
| GET | `/api/auth/me` | Public | 无 | 无 | `IdentityVO` | 200, 500 | `INTERNAL_ERROR` | 初始化 Owner Mode；匿名返回 200/VISITOR |
| POST | `/api/auth/logout` | Authenticated | 无 | 无 | 无 | 204, 401, 500 | `AUTH_REQUIRED`, `INTERNAL_ERROR` | 撤销当前 Redis Token Session |

认证流程：登录获得 Token → 前端仅在当前认证状态中管理 Token → 请求携带 Bearer Token → Redis 校验 Token Session → 退出时撤销。前端收到 401 后清空身份并回到匿名 Visitor。V2 暂不引入 Refresh Token；过期后重新登录。

## 4. Site Bootstrap、Profile 与配置

### 4.1 VO

```text
SiteBootstrapVO {
  basic: SiteBasicVO
  profile: ProfileVO
  hero: HeroVO
  announcement: AnnouncementVO | null
  music: MusicVO | null
  appearance: AppearanceVO
  pageMedia: PageMediaVO
  externalLinks: ExternalLinksVO
  updatedAt: string
  version: number                 // site_config 共享版本
}

SiteConfigSectionVO<T> {
  config: T
  version: number
  updatedAt: string
}

SiteBasicVO {
  name: string
  chineseName: string
  description: string | null
}

ProfileVO {
  id: string
  userId: string
  name: string
  avatar: string | null
  role: string | null
  bio: string | null
  status: { label: string | null, text: string | null, emoji: string | null }
  stats: { techPostCount: number, momentCount: number, guestbookCount: number }
  version: number                 // profile 自身版本
  updatedAt: string
}

HeroVO {
  eyebrow: string | null
  heroTitle: string
  heroSubtitle: string | null
  desktopVideo: string | null
  mobileVideo: string | null
  poster: string | null
}

AnnouncementVO { content: string, enabled: boolean }

MusicVO {
  title: string
  artist: string | null
  audioUrl: string
  cover: string | null
}

AppearanceVO {
  layoutMode: "standard" | "immersive"
  backgroundMode: "video" | "wallpaper"
  surfaceOpacity: number          // 0..1
  backdropShade: number           // 0..1
  wallpaper: { desktop: string | null, mobile: string | null }
  allowVisitorControls: boolean
}

PageMediaItemVO {
  video: string | null
  poster: string | null
  overlay: string | null
  motion: string | null
  effect: string | null
}

PageMediaVO {
  moments: PageMediaItemVO
  guestbook: PageMediaItemVO
  tech: PageMediaItemVO
  archive: PageMediaItemVO
  about: PageMediaItemVO
}

ExternalLinksVO { homepage: string | null, github: string | null }
```

无启用公告时 `announcement=null`；缺少 audioUrl 时 `music=null`。不得返回伪造公告、音乐、统计或内容。

### 4.2 更新 DTO

```text
UpdateProfileDTO {
  name: string                     // 1..64
  avatar: string | null            // <=1024
  role: string | null              // <=128
  bio: string | null               // <=500
  status: {
    label: string | null            // <=64
    text: string | null             // <=255
    emoji: string | null            // <=32
  }
  version: number
}

UpdateSiteBasicDTO {
  name: string                     // 1..64
  chineseName: string              // 1..64
  description: string | null       // <=255
  version: number
}

UpdateHeroDTO {
  eyebrow: string | null           // <=128
  heroTitle: string                // 1..180
  heroSubtitle: string | null      // <=500
  desktopVideo: string | null      // <=1024
  mobileVideo: string | null       // <=1024
  poster: string | null            // <=1024
  version: number
}

UpdateAnnouncementDTO {
  content: string | null           // <=2000
  enabled: boolean
  version: number
}

UpdateMusicDTO {
  title: string | null             // <=180
  artist: string | null            // <=180
  audioUrl: string | null          // <=1024
  cover: string | null             // <=1024
  version: number
}

UpdateAppearanceDTO extends AppearanceVO { version: number }

UpdatePageMediaDTO {
  moments: PageMediaItemVO
  guestbook: PageMediaItemVO
  tech: PageMediaItemVO
  archive: PageMediaItemVO
  about: PageMediaItemVO
  version: number
}
```

公告 enabled=true 时 content 必须非空；当前前端空内容保存应发送 enabled=false。Music audioUrl 为空表示关闭，公开 Bootstrap 返回 null。

### 4.3 Site / Profile 接口

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/site/bootstrap` | Public | 无 | 无 | `SiteBootstrapVO` | 200, 500 | `INTERNAL_ERROR` | Navbar、Hero、Profile、公告、音乐、背景、About 外链 |
| PUT | `/api/owner/profile` | OWNER + `PROFILE_EDIT` | 无 | `UpdateProfileDTO` | `ProfileVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | Settings 保存资料、Profile 编辑 |
| PUT | `/api/owner/site/basic` | OWNER + `SITE_EDIT` | 无 | `UpdateSiteBasicDTO` | `SiteConfigSectionVO<SiteBasicVO>` | 200, 400, 401, 403, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | Settings 保存基本信息 |
| PUT | `/api/owner/site/hero` | OWNER + `SITE_EDIT` | 无 | `UpdateHeroDTO` | `SiteConfigSectionVO<HeroVO>` | 200, 400, 401, 403, 409, 500 | 同上 | Hero 更换背景、Settings 保存 Hero |
| PUT | `/api/owner/site/announcement` | OWNER + `SITE_EDIT` | 无 | `UpdateAnnouncementDTO` | `SiteConfigSectionVO<AnnouncementVO | null>` | 200, 400, 401, 403, 409, 500 | 同上 | 编辑公告 |
| PUT | `/api/owner/site/music` | OWNER + `SITE_EDIT` | 无 | `UpdateMusicDTO` | `SiteConfigSectionVO<MusicVO | null>` | 200, 400, 401, 403, 409, 500 | 同上 | 编辑音乐 |
| PUT | `/api/owner/site/appearance` | OWNER + `SITE_EDIT` | 无 | `UpdateAppearanceDTO` | `SiteConfigSectionVO<AppearanceVO>` | 200, 400, 401, 403, 409, 500 | 同上 | 保存空间默认外观；当前 AppearanceControls 仍是本次浏览偏好 |
| PUT | `/api/owner/site/page-media` | OWNER + `SITE_EDIT` | 无 | `UpdatePageMediaDTO` | `SiteConfigSectionVO<PageMediaVO>` | 200, 400, 401, 403, 409, 500 | 同上 | 保存各页面默认背景；当前 Settings 尚未暴露表单 |

`externalLinks` 当前 Owner UI 没有编辑动作，V1 只在 Bootstrap 读取，不设计写接口。

### 4.4 SiteConfig 共享乐观锁

Basic、Hero、Announcement、Music、Appearance、PageMedia 共用 `site_config.singleton_key='PRIMARY'` 同一行和同一个 version：

1. 每个更新 DTO 携带当前共享 version。
2. 服务端按 id + version 条件更新；成功后 version+1 并将 updated_by 写为真实 Owner ID。
3. 成功响应必须返回最新 `SiteConfigSectionVO.version`。
4. 前端立即覆盖本地版本，下一项配置使用新版本。
5. 冲突返回 `409 VERSION_CONFLICT` 和 `currentVersion`；重新获取 Bootstrap，不静默覆盖。

Profile 使用自己的 `profile.version`，不影响 SiteConfig。

## 5. Post 模型与 DTO / VO

```text
PostAuthorVO { userId: string, name: string, avatar: string | null }

MediaVO {
  id: string
  usageType: "COVER" | "CONTENT"
  mediaType: "IMAGE" | "VIDEO"
  src: string
  poster: string | null
  alt: string | null
  width: number | null
  height: number | null
  sortOrder: number
}

MediaInputDTO {
  src: string                       // 1..1024
  mediaType: "IMAGE" | "VIDEO"
  poster: string | null             // <=1024
  alt: string | null                // <=255
  width: number | null              // >0
  height: number | null             // >0
  sortOrder: number                 // >=0
}

CategoryVO {
  id: string
  name: string
  slug: string
  description: string | null
  active: boolean
}

TagVO { id: string, name: string, slug: string, active: boolean }

PostSummaryVO {
  id: string
  type: "TECH" | "MOMENT"
  slug: string | null
  title: string | null
  summary: string | null
  content: string | null             // MOMENT 列表正文；TECH 列表 null
  author: PostAuthorVO
  category: CategoryVO | null
  tags: TagVO[]
  cover: MediaVO | null              // 仅 TECH
  images: MediaVO[]                  // 仅 MOMENT；TECH=[]
  createdAt: string
  updatedAt: string
  publishedAt: string | null
  readingTime: number | null
  status: "DRAFT" | "PUBLISHED" | null
  likeCount: number
  commentCount: number
  version: number | null
}

TechDetailVO extends PostSummaryVO {
  type: "TECH"
  slug: string
  title: string
  summary: string
  content: string
  contentFormat: "MARKDOWN" | "PLAIN_TEXT"
  category: CategoryVO
  readingTime: number
  relatedPosts: PostSummaryVO[]
}

MomentDetailVO extends PostSummaryVO {
  type: "MOMENT"
  slug: null
  title: null
  summary: null
  category: null
  tags: []
  cover: null
  readingTime: null
  contentFormat: "PLAIN_TEXT"
  content: string
}

CreateTechPostDTO {
  title: string                      // 1..180
  summary: string                    // 1..600
  content: string                    // 非空
  contentFormat: "MARKDOWN" | "PLAIN_TEXT"
  categoryId: string
  tagIds: string[]                   // 0..10，去重
  cover: MediaInputDTO | null
  status: "DRAFT" | "PUBLISHED"
}

UpdateTechPostDTO extends CreateTechPostDTO { version: number }

CreateMomentDTO {
  content: string                    // 1..2000
  images: MediaInputDTO[]            // 0..9
  status: "DRAFT" | "PUBLISHED"
}

UpdateMomentDTO extends CreateMomentDTO { version: number }
```

Visitor VO 中 status 可固定 PUBLISHED、version=null；Owner 必须返回真实值。MOMENT 不得生成 title、summary、category、tags 或 readingTime。

前端不能提交 id、slug、readingTime、createdAt、updatedAt、publishedAt、createdBy、updatedBy、likeCount、commentCount：

- TECH slug 创建时服务端生成并保证唯一；修改标题不改 slug。
- readingTime 服务端计算。
- DRAFT 首次发布时服务端设置 publishedAt；PUBLISHED 撤回 DRAFT 时置空。
- TECH Markdown 正文图片 URL 直接保存在 content；只有 Cover 写 `post_media`。
- MOMENT 图片全部写 `post_media`，usageType=CONTENT。
- PUT 是完整替换；标签、媒体关联在同一事务替换。

## 6. Visitor Post 与 Archive API

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/posts` | Public | Query: `type=ALL|TECH|MOMENT`, `page`, `pageSize` | 无 | `PageVO<PostSummaryVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | 首页 Feed 筛选和查看更多 |
| GET | `/api/posts/tech` | Public | Query: `category` slug、`tag` slug、`page`, `pageSize` | 无 | `PageVO<PostSummaryVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | `/tech` 列表、分类/标签筛选 |
| GET | `/api/posts/tech/{slug}` | Public | Path: `slug` | 无 | `TechDetailVO` | 200, 404, 500 | `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | TECH 详情、相关文章 |
| GET | `/api/posts/moments` | Public | Query: `year`、`month=1..12` 可选，`page`, `pageSize` | 无 | `PageVO<PostSummaryVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | `/moments` 时间流；前端分月 |
| GET | `/api/posts/moments/{id}` | Public | Path: `id` string | 无 | `MomentDetailVO` | 200, 400, 404, 500 | `BAD_REQUEST`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | MOMENT 详情 |
| GET | `/api/archive` | Public | Query: `year` 可选、`type=ALL|TECH|MOMENT`、`page`, `pageSize` | 无 | `ArchiveVO` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | TECH/MOMENT 混合归档 |

只返回 `deleted=0 AND status='PUBLISHED' AND published_at<=NOW()`；Visitor 请求草稿或删除内容统一 404。

```text
ArchiveItemVO {
  id: string
  type: "TECH" | "MOMENT"
  slug: string | null
  label: string             // TECH=title；MOMENT=正文安全截断
  publishedAt: string
}

ArchiveVO {
  groups: [{ year: number, months: [{ month: number, items: ArchiveItemVO[] }] }]
  page: number
  pageSize: number
  total: number
  totalPages: number
  hasMore: boolean
}
```

## 7. Owner Post API

### 7.1 Owner 读取

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/owner/posts` | OWNER | Query: `type=ALL|TECH|MOMENT`, `status=ALL|DRAFT|SCHEDULED|PUBLISHED`, `page`, `pageSize` | 无 | `PageVO<PostSummaryVO>` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 刷新后恢复草稿/定时/已发布列表 |
| GET | `/api/owner/posts/tech/{id}` | OWNER + `POST_EDIT` | Path: `id` | 无 | `TechDetailVO` | 200, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 按 ID 恢复 TECH 编辑器 |
| GET | `/api/owner/posts/moments/{id}` | OWNER + `POST_EDIT` | Path: `id` | 无 | `MomentDetailVO` | 200, 400, 401, 403, 404, 500 | 同上 | 按 ID 恢复 MOMENT 编辑器 |

Owner 返回所有未删除 DRAFT/PUBLISHED，默认按 updatedAt DESC/id DESC，强制包含 status 和 version。

### 7.2 TECH CRUD

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/owner/posts/tech` | OWNER + `POST_CREATE` | 无 | `CreateTechPostDTO` | `TechDetailVO` | 201, 400, 401, 403, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | Navbar/Tech「写文章」 |
| PUT | `/api/owner/posts/tech/{id}` | OWNER + `POST_EDIT` | Path: `id` | `UpdateTechPostDTO` | `TechDetailVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | TECH Card/Detail 编辑 |
| DELETE | `/api/owner/posts/tech/{id}` | OWNER + `POST_DELETE` | Path: `id`; Query: `version` 必填 | 无 | 无 | 204, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | TECH 确认删除 |

### 7.3 MOMENT CRUD

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/owner/posts/moments` | OWNER + `POST_CREATE` | 无 | `CreateMomentDTO` | `MomentDetailVO` | 201, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | Navbar/Moments「发说说」 |
| PUT | `/api/owner/posts/moments/{id}` | OWNER + `POST_EDIT` | Path: `id` | `UpdateMomentDTO` | `MomentDetailVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | MOMENT Card/Detail 编辑 |
| DELETE | `/api/owner/posts/moments/{id}` | OWNER + `POST_DELETE` | Path: `id`; Query: `version` 必填 | 无 | 无 | 204, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | MOMENT 确认删除 |

Post 删除统一为 `DELETE .../{id}?version=3`；不使用 If-Match、ETag 或自定义 Version Header。删除确认只属于前端 UI，不进入 DTO。

## 8. Category / Tag API

```text
CategoryAdminVO extends CategoryVO {
  sortOrder: number
  version: number
  updatedAt: string
}

TagAdminVO extends TagVO { updatedAt: string }

CreateCategoryDTO {
  name: string              // 1..64
  slug: string | null       // null 时服务端生成，<=80
  description: string | null
  sortOrder: number
}

UpdateCategoryDTO {
  name: string
  slug: string
  description: string | null
  sortOrder: number
  version: number
}

CreateTagDTO { name: string, slug: string | null }
UpdateTagDTO { name: string, slug: string }
```

数据库 category 有 version，更新/删除使用乐观锁；冻结的 tag 表没有 version，V1 不虚构字段，使用唯一约束处理冲突。

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/categories` | Public | `used=true|false` 可选、`page`, `pageSize` | 无 | `PageVO<CategoryVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | Tech 分类筛选 |
| GET | `/api/tags` | Public | `used=true|false` 可选、`page`, `pageSize` | 无 | `PageVO<TagVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | Tech 标签筛选/Space Tags |
| GET | `/api/owner/categories` | OWNER + `CATEGORY_MANAGE` | `status=ACTIVE|DISABLED|ALL`, `page`, `pageSize` | 无 | `PageVO<CategoryAdminVO>` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 分类管理/文章候选项 |
| POST | `/api/owner/categories` | OWNER + `CATEGORY_MANAGE` | 无 | `CreateCategoryDTO` | `CategoryAdminVO` | 201, 400, 401, 403, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 新建分类 |
| PUT | `/api/owner/categories/{id}` | OWNER + `CATEGORY_MANAGE` | Path: `id` | `UpdateCategoryDTO` | `CategoryAdminVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 修改分类 |
| DELETE | `/api/owner/categories/{id}` | OWNER + `CATEGORY_MANAGE` | Path: `id`; Query: `version` | 无 | 无 | 204, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | 逻辑停用分类 |
| GET | `/api/owner/tags` | OWNER + `TAG_MANAGE` | `status=ACTIVE|DISABLED|ALL`, `page`, `pageSize` | 无 | `PageVO<TagAdminVO>` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 标签管理/文章候选项 |
| POST | `/api/owner/tags` | OWNER + `TAG_MANAGE` | 无 | `CreateTagDTO` | `TagAdminVO` | 201, 400, 401, 403, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 新建标签 |
| PUT | `/api/owner/tags/{id}` | OWNER + `TAG_MANAGE` | Path: `id` | `UpdateTagDTO` | `TagAdminVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 修改标签 |
| DELETE | `/api/owner/tags/{id}` | OWNER + `TAG_MANAGE` | Path: `id` | 无 | 无 | 204, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 逻辑停用标签 |

公开列表和 Owner 新建候选项只返回 deleted=0。历史 TECH Detail 必须从既有关联返回原名称并标 active=false；编辑旧文章可展示停用项，但保存前必须替换或先恢复。V1 暂不设计恢复接口。停用不清空 post.category_id 或 post_tag。

## 9. Comment API（属于 Post）

```text
ReplyDTO { content: string }       // 1..2000

ReplyVO {
  id: string
  author: { userId: string, name: string, avatar: string | null }
  content: string
  createdAt: string
}

CommentVO {
  id: string
  postId: string
  author: { name: string, avatar: string | null }
  content: string
  createdAt: string
  ownedByMe: boolean
  ownerReply: ReplyVO | null
}
```

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/posts/{postId}/comments` | Public | Path: `postId`; `page`, `pageSize` | 无 | `PageVO<CommentVO>` | 200, 400, 404, 500 | `BAD_REQUEST`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | TECH/MOMENT Detail 评论 |
| POST | `/api/owner/comments/{commentId}/reply` | OWNER + `COMMENT_REPLY` | Path: `commentId` | `ReplyDTO` | `CommentVO` | 201/200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 回复评论；新建 201，恢复删除回复 200 |
| DELETE | `/api/owner/comments/{commentId}` | OWNER + `COMMENT_DELETE` | Path: `commentId` | 无 | 无 | 204, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 删除评论 |

Comment 必须属于 Post；每条顶层评论最多一个 Owner 回复。已有未删除回复再次回复返回 409。删除顶层评论时软删回复并事务维护 post.comment_count。V1 不提供 Visitor 创建评论。

## 10. Guestbook API（属于整个空间）

```text
GuestbookVO {
  id: string
  author: { name: string, avatar: string | null }
  content: string
  createdAt: string
  ownedByMe: boolean
  ownerReply: ReplyVO | null
}
```

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 对应前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/guestbook` | Public | `page`, `pageSize` | 无 | `PageVO<GuestbookVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | 留言板；首页预览用 page=1&pageSize=3 |
| POST | `/api/owner/guestbook/{entryId}/reply` | OWNER + `GUESTBOOK_REPLY` | Path: `entryId` | `ReplyDTO` | `GuestbookVO` | 201/200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 回复留言；新建 201，恢复删除回复 200 |
| DELETE | `/api/owner/guestbook/{entryId}` | OWNER + `GUESTBOOK_DELETE` | Path: `entryId` | 无 | 无 | 204, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 删除留言 |

Guestbook 不接受 postId，也不能复用 Comment Controller/Service/DTO。V1 不提供 Visitor 创建留言。

## 11. 前端路由 → API

| 前端位置 | Visitor 读取 | Owner 额外操作 |
|---|---|---|
| 全局 App/Navbar | auth/me、site/bootstrap | 登录、退出，显示发布/设置 |
| `/` | posts?type=ALL、guestbook?pageSize=3 | 按类型编辑/删除 Post |
| `/moments` | posts/moments | Owner 列表和 MOMENT CRUD |
| `/moments/:id` | Moment detail、comments | Owner 按 ID 读取、编辑/删除、评论管理 |
| `/tech` | posts/tech、categories、tags | Owner 列表和 TECH CRUD |
| `/tech/:slug` | Tech detail、comments | Owner 按响应 id 读取编辑数据及评论管理 |
| `/guestbook` | guestbook | 回复/删除留言 |
| `/archive` | archive | 无 |
| `/about` | site/bootstrap | Profile/Basic 在 Settings 修改 |
| `/settings` | site/bootstrap | Profile、Hero、Announcement、Music、Basic 更新 |

## 12. 当前前端 Mock / Runtime → API 映射

| Vue 来源 / 方法 | 未来 API | 迁移说明 |
|---|---|---|
| `useOwnerMode.isOwner` | GET auth/me | 只信服务端身份，删除 query 参数身份信任 |
| `siteMeta`, `updateBasic()` | Bootstrap basic；PUT owner/site/basic | site_config 共享 version |
| `profile.js`, `updateProfile()` | Bootstrap profile；PUT owner/profile | Profile 自身 version，stats 聚合 |
| `heroContent`, `updateHero()` | Bootstrap hero；PUT owner/site/hero | description 映射 heroSubtitle |
| `announcement`, `updateAnnouncement()` | Bootstrap announcement；PUT owner/site/announcement | 空内容发送 enabled=false |
| `musicMedia`, `updateMusic()` | Bootstrap music；PUT owner/site/music | src 统一为 audioUrl |
| `appearanceConfig`, AppearanceControls | Bootstrap appearance | 当前控件只改变本次浏览；保存默认值才 PUT |
| `pageMedia` | Bootstrap pageMedia；PUT owner/site/page-media | 媒体路径不散落组件 |
| `externalLinks` | Bootstrap externalLinks | V1 Owner UI 只读 |
| `posts.js`, runtime posts | Visitor posts；Owner posts | 空 Mock 替换为 PageVO |
| `createTech()` | POST owner/posts/tech | category/tag 字符串改 categoryId/tagIds；后端生成 slug/readingTime |
| `updatePost()` TECH | PUT owner/posts/tech/{id} | 携带 version，成功 VO 替换当前项 |
| `createMoment()` | POST owner/posts/moments | 不提交 title |
| `updatePost()` MOMENT | PUT owner/posts/moments/{id} | 携带 version |
| `deletePost()` | 对应 DELETE + version | 204 后才从列表移除 |
| TECH `images[0]` | cover MediaVO | `post_media.COVER` |
| MOMENT `images` | images MediaVO[] | `post_media.CONTENT` |
| `comments.js` | GET posts/{postId}/comments | 空 Mock 替换 PageVO |
| `replyComment()` / `deleteComment()` | owner/comments APIs | 不与 Guestbook 共用 |
| `guestbook.js` | GET guestbook | 首页复用 pageSize=3 |
| `replyGuestbook()` / `deleteGuestbook()` | owner/guestbook APIs | 使用独立 VO/API |
| 刷新后丢失草稿 | GET owner/posts + Owner 按 ID | DRAFT/PUBLISHED 均恢复 |

接入后端后，前端不得生成 ID、slug、时间、阅读时间或互动计数，也不得用 localStorage/IndexedDB 假持久化。

## 13. 接口 → 数据表对应关系

| 业务 / 接口组 | 主要表 | 说明 |
|---|---|---|
| Auth | `space_user`, `profile` | JWT 自包含身份，Redis 保存可撤销 Token Session，不建 MySQL token 表 |
| Site Bootstrap | `site_config`, `profile`, `post`, `guestbook` | stats 聚合，不造数据 |
| Update Profile | `profile`, `space_user` | profile version |
| Site 更新 | `site_config`, `space_user` | PRIMARY 行，共享 version，updated_by=Owner |
| Feed / Archive | `post`, `category`, `post_tag`, `tag`, `post_media`, `profile` | 公开 PUBLISHED |
| TECH Detail | `post`, `category`, `post_tag`, `tag`, `post_media`, `profile` | 已停用名称仍返回 |
| MOMENT Detail | `post`, `post_media`, `profile` | 不读 TECH 专属字段 |
| Create/Update TECH | `post`, `category`, `tag`, `post_tag`, `post_media`, `space_user` | 标签与 Cover 同事务；正文图片在 content |
| Create/Update MOMENT | `post`, `post_media`, `space_user` | Post 与图片同事务 |
| Delete Post | `post`, `post_media`, `space_user` | 逻辑删除 |
| Category | `category`, `post` | 停用不清历史 FK |
| Tag | `tag`, `post_tag` | 停用不删历史关联 |
| Comment | `comment`, `post`, `space_user`, `profile` | 回复同表 parent_id，维护 comment_count |
| Guestbook | `guestbook`, `space_user`, `profile` | 独立于 Post/Comment |

这里只规定关系，不规定 SQL 或 Mapper。

## 14. 事务、版本与安全要求

- Create/Update TECH：Post、Category/Tag 有效性、PostTag、Cover 同一事务。
- Create/Update MOMENT：Post 与 PostMedia 同一事务。
- Delete Post：Post 与 PostMedia 逻辑删除同一事务。
- Reply Comment/Guestbook：父记录、深度、已有回复检查和写入/恢复同一事务。
- Delete Comment：评论链软删与 commentCount 更新同一事务。
- 有 version 列的 Post、Profile、Category、SiteConfig 必须条件匹配版本。
- Post/Category DELETE 使用 version Query，不用 If-Match/ETag。
- Comment、Guestbook、Tag 表没有 version，API 不虚构版本。
- 创建接口可接受可选 `Idempotency-Key`，但不依赖它保证正确性。
- 密码、JWT、Redis Token key 和 Authorization Header 禁止写日志。
- Owner 写接口同时要求有效 Bearer Token、Redis Token Session 与 Permission；隐藏按钮不是鉴权。
- 新选择 Category/Tag 必须 active；历史读取不能过滤已关联的逻辑删除名称。

## 15. Owner 业务动作覆盖检查

| Owner 动作 | 最终 API |
|---|---|
| Identity / Login / Logout | auth/me、auth/login、auth/logout |
| Read Space Configuration | site/bootstrap |
| Read Draft/Published | owner/posts |
| Read TECH/MOMENT by ID | owner/posts/tech/{id}、owner/posts/moments/{id} |
| TECH CRUD | owner/posts/tech... |
| MOMENT CRUD | owner/posts/moments... |
| Update Profile | PUT owner/profile |
| Update Basic/Hero/Announcement/Music | PUT owner/site/... |
| Comment reply/delete | owner/comments/{id}... |
| Guestbook reply/delete | owner/guestbook/{id}... |
| Category/Tag manage | owner/categories...、owner/tags... |

当前 Visitor 页面、Owner UI Mode、TECH、MOMENT、Profile、Hero、Music、Announcement、Comment、Guestbook、Settings 和 Archive 所需契约均已覆盖，可以据此实现后端而无需重新猜测字段、权限或数据表边界。

## 16. 功能 2：Visitor 账号与互动

### 16.1 数据与权限前提

- `space_user.role` 后续需允许 `VISITOR`，但仍只有 `VISITOR` 与 `OWNER` 两种角色。
- 匿名 Visitor 只能读；登录 Visitor 才能新增、修改、删除自己的评论和留言。
- Owner 仍使用现有回复/管理接口。Visitor 不能回复别人、不能删除别人内容。
- 评论与留言的作者从 JWT Principal 取得，前端不得提交 `authorUserId`、作者名或头像。
- 删除采用软删除；修改与删除必须同时匹配 `author_user_id=currentUserId`。

```text
RegisterVisitorDTO {
  username: string       // 3..64，唯一
  password: string       // 8..128
  displayName: string    // 1..64
}

CreateCommentDTO { content: string }       // 1..2000
UpdateCommentDTO { content: string }       // 1..2000
CreateGuestbookDTO { content: string }     // 1..2000
UpdateGuestbookDTO { content: string }     // 1..2000
```

| Method | Path | Permission | Query / Path Variable | Request DTO | Response VO | Status | Error Code | 前端功能 |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/auth/register` | Public | 无 | `RegisterVisitorDTO` | `LoginVO` | 201, 400, 409, 500 | `BAD_REQUEST`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | Visitor 注册并直接获得 JWT |
| POST | `/api/posts/{postId}/comments` | Authenticated VISITOR/OWNER | `postId:string` | `CreateCommentDTO` | `CommentVO` | 201, 400, 401, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 文章/说说发表评论 |
| PUT | `/api/comments/{commentId}` | Comment Owner | `commentId:string` | `UpdateCommentDTO` | `CommentVO` | 200, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | Visitor 修改自己的评论 |
| DELETE | `/api/comments/{commentId}` | Comment Owner | `commentId:string` | 无 | 无 | 204, 401, 403, 404, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | Visitor 删除自己的评论 |
| POST | `/api/guestbook` | Authenticated VISITOR/OWNER | 无 | `CreateGuestbookDTO` | `GuestbookVO` | 201, 400, 401, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 发布空间留言 |
| PUT | `/api/guestbook/{entryId}` | Entry Owner | `entryId:string` | `UpdateGuestbookDTO` | `GuestbookVO` | 200, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | Visitor 修改自己的留言 |
| DELETE | `/api/guestbook/{entryId}` | Entry Owner | `entryId:string` | 无 | 无 | 204, 401, 403, 404, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | Visitor 删除自己的留言 |

注册、写评论和写留言必须限流。用户名冲突返回 409；目标 Post 不公开时发表评论返回 404。Visitor 删除顶层评论时同时软删其 Owner 回复并事务递减 `post.comment_count`。

## 17. 功能 3：持久化点赞

点赞必须有独立关系记录，不能只对 `post.like_count` 做 `+1/-1`。同一用户对同一 Post 最多一条有效关系，匿名用户暂不允许点赞。

```text
LikeStateVO {
  postId: string
  liked: boolean
  likeCount: number
}
```

| Method | Path | Permission | Parameters | Request | Response | Status | Error Code | 前端功能 |
|---|---|---|---|---|---|---|---|---|
| PUT | `/api/posts/{postId}/like` | Authenticated | `postId:string` | 无 | `LikeStateVO` | 200, 401, 404, 500 | `AUTH_REQUIRED`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 幂等点赞 |
| DELETE | `/api/posts/{postId}/like` | Authenticated | `postId:string` | 无 | `LikeStateVO` | 200, 401, 404, 500 | `AUTH_REQUIRED`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 幂等取消点赞 |
| GET | `/api/me/likes` | Authenticated | `page`, `pageSize` | 无 | `PageVO<PostSummaryVO>` | 200, 400, 401, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `INTERNAL_ERROR` | 我的点赞列表 |

点赞/取消点赞在一个事务中维护关系表与 `post.like_count`。重复 PUT/DELETE 不报冲突，返回当前最终状态。Post 列表和详情登录时增加 `likedByMe:boolean`；匿名时固定为 `false`。

## 18. 功能 4：Owner 媒体上传与 OSS

V2 采用“后端接收 multipart → 校验 → 上传 OSS → 保存媒体资产 → 返回 URL”的简单链路。AccessKey/Secret 只存在后端本地配置，前端永远看不到。当前 URL 字段保留，上传成功后把返回 URL 填入 Profile、Hero、Music、PageMedia 或 Post DTO。

```text
MediaAssetVO {
  id: string
  mediaType: "IMAGE" | "VIDEO" | "AUDIO"
  usageType: "AVATAR" | "HERO" | "POST_COVER" | "POST_CONTENT" | "MUSIC" | "PAGE_BACKGROUND"
  url: string
  originalName: string
  contentType: string
  size: number
  width: number | null
  height: number | null
  durationSeconds: number | null
  createdAt: string
}
```

| Method | Path | Permission | Parameters | Request | Response | Status | Error Code | 前端功能 |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/owner/media` | OWNER + `MEDIA_MANAGE` | 无 | `multipart/form-data`: `file`, `usageType` | `MediaAssetVO` | 201, 400, 401, 403, 413, 415, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `PAYLOAD_TOO_LARGE`, `UNSUPPORTED_MEDIA_TYPE`, `INTERNAL_ERROR` | 上传头像、背景、封面、说说图片、音乐 |
| GET | `/api/owner/media` | OWNER + `MEDIA_MANAGE` | `mediaType?`, `usageType?`, `page`, `pageSize` | 无 | `PageVO<MediaAssetVO>` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 选择已有媒体 |
| DELETE | `/api/owner/media/{mediaId}` | OWNER + `MEDIA_MANAGE` | `mediaId:string` | 无 | 无 | 204, 401, 403, 404, 409, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 删除未被引用的媒体 |

文件校验以服务端 MIME 探测为准，不能只信扩展名。被 Profile/SiteConfig/Post 引用的媒体返回 409，不直接删除 OSS 对象。文件大小与格式限制由配置控制，并在错误 message 中返回可理解原因。

## 19. 功能 5：实时通知

BinSpace 不是聊天室。V2 只实时推送“新评论、新留言、Owner 回复”等事件，不把评论 CRUD 改成 WebSocket。HTTP 负责可靠写入，SSE 负责提醒；断线后通过通知列表补齐。

```text
NotificationVO {
  id: string
  type: "COMMENT_CREATED" | "GUESTBOOK_CREATED" | "COMMENT_REPLIED" | "GUESTBOOK_REPLIED"
  actor: VisitorAuthorVO | null
  resourceType: "POST" | "COMMENT" | "GUESTBOOK"
  resourceId: string
  summary: string
  read: boolean
  createdAt: string
}
```

| Method | Path | Permission | Parameters | Request | Response | Status | Error Code | 前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/realtime/events` | Authenticated | `Last-Event-ID` header 可选 | 无 | `text/event-stream` | 200, 401, 500 | `AUTH_REQUIRED`, `INTERNAL_ERROR` | 实时接收通知 |
| GET | `/api/notifications` | Authenticated | `unreadOnly=false`, `page`, `pageSize` | 无 | `PageVO<NotificationVO>` | 200, 400, 401, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `INTERNAL_ERROR` | 通知列表/断线补偿 |
| PUT | `/api/notifications/{notificationId}/read` | Notification Owner | `notificationId:string` | 无 | `NotificationVO` | 200, 401, 403, 404, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 标记单条已读 |
| PUT | `/api/notifications/read-all` | Authenticated | 无 | 无 | `UnreadCountVO` | 200, 401, 500 | `AUTH_REQUIRED`, `INTERNAL_ERROR` | 全部已读 |

SSE 事件 `id` 使用通知 ID，`event` 使用类型，`data` 为 `NotificationVO` JSON。心跳可以是注释帧；前端指数退避重连。SSE 不替代数据库通知记录。

## 20. 功能 6：Redis 业务缓存

本功能不新增前端 API。缓存是 Service/Repository 之间的透明实现，不允许创建“清缓存”按钮或把缓存命中信息暴露给 Visitor。

| Key 模式 | 内容 | 建议 TTL | 失效时机 |
|---|---|---:|---|
| `binspace:site:bootstrap` | SiteBootstrapVO | 10 min | 任一 Owner Site/Profile 更新成功后删除 |
| `binspace:post:tech:{slug}` | 公开 TECH Detail | 10 min | 对应 TECH 更新、删除、发布/撤回后删除 |
| `binspace:post:moment:{id}` | 公开 MOMENT Detail | 5 min | 对应 MOMENT 更新、删除、发布/撤回后删除 |
| `binspace:feed:{type}:{page}:{pageSize}` | 公开 Feed 页 | 2 min | 任一 Post 发布、更新、删除后按命名空间失效 |
| `binspace:taxonomy:categories` | 公开分类 | 10 min | Category 或 TECH 关联变化后删除 |
| `binspace:taxonomy:tags` | 公开标签 | 10 min | Tag 或 PostTag 变化后删除 |
| `binspace:like:count:{postId}` | 点赞计数热点值 | 1 min | 点赞写入后更新/删除 |
| `binspace:view:dedupe:{postId}:{fingerprint}` | 阅读去重标记 | 30 min | TTL 自动过期 |

必须遵守 Cache-Aside：先提交数据库事务，再删除缓存；绝不能在事务提交前写入新缓存。空结果只短暂缓存以防穿透；禁止缓存密码、JWT、未发布正文和 Owner 编辑 DTO。

## 21. 功能 7：搜索、推荐与 AI 搜索

```text
SearchResultVO {
  id: string
  type: "TECH" | "MOMENT"
  slug: string | null
  title: string | null
  excerpt: string
  highlights: string[]
  publishedAt: string
}

AiSearchDTO { question: string }       // 1..500
AiSearchVO {
  answer: string
  sources: [{ postId: string, slug: string | null, title: string, excerpt: string }]
}

ReindexTaskVO {
  taskId: string
  status: "QUEUED" | "RUNNING" | "SUCCEEDED" | "FAILED"
  createdAt: string
}
```

| Method | Path | Permission | Parameters | Request | Response | Status | Error Code | 前端功能 |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/search` | Public | `q` 必填；`type=ALL|TECH|MOMENT`; `category?`; `tag?`; `page`; `pageSize` | 无 | `PageVO<SearchResultVO>` | 200, 400, 500 | `BAD_REQUEST`, `INTERNAL_ERROR` | 全文搜索 |
| GET | `/api/posts/{postId}/related` | Public | `postId:string`, `limit=1..10` | 无 | `PostSummaryVO[]` | 200, 400, 404, 500 | `BAD_REQUEST`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 相关文章/动态推荐 |
| POST | `/api/search/ai` | Public（限流） | 无 | `AiSearchDTO` | `AiSearchVO` | 200, 400, 429, 502, 500 | `BAD_REQUEST`, `RATE_LIMITED`, `UPSTREAM_ERROR`, `INTERNAL_ERROR` | 基于已发布内容问答 |
| POST | `/api/owner/search/reindex` | OWNER + `SEARCH_REINDEX` | 无 | 无 | `ReindexTaskVO` | 202, 401, 403, 409, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 重建 Elasticsearch 索引 |
| GET | `/api/owner/search/reindex/{taskId}` | OWNER + `SEARCH_REINDEX` | `taskId:string` | 无 | `ReindexTaskVO` | 200, 401, 403, 404, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 查看重建进度 |

只索引公开 PUBLISHED 内容；草稿、软删除内容和私有字段不得进入公开搜索或 AI 上下文。相关文章第一版按同分类/标签和时间衰减排序，不记录个性化画像。AI 回答必须携带来源，找不到依据时明确返回无法回答。

## 22. 功能 8：内容高级能力

新增 Post 状态 `SCHEDULED`；自动保存与历史版本不得覆盖正式 Post。所有恢复、改 slug、定时发布和批量操作继续执行类型规则、权限校验与乐观锁。

```text
SchedulePostDTO { publishAt: string, version: number }
AutosavePostDTO { type: "TECH" | "MOMENT", payload: object, baseVersion: number }
AutosaveVO { postId: string, savedAt: string, baseVersion: number }
PostVersionVO { id: string, postId: string, versionNo: number, summary: string, createdAt: string }
RestoreVersionDTO { version: number }
BatchPostDTO { items: [{ postId: string, version: number }] }
BatchPostResultVO { succeededIds: string[], failed: [{ postId: string, code: string, message: string }] }
UpdateSlugDTO { slug: string, version: number }
MarkdownImportVO { title: string, content: string, frontMatter: object }
```

| Method | Path | Permission | Parameters | Request | Response | Status | Error Code | 前端功能 |
|---|---|---|---|---|---|---|---|---|
| PUT | `/api/owner/posts/{postId}/schedule` | OWNER + `POST_EDIT` | `postId:string` | `SchedulePostDTO` | `PostSummaryVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | 设置/修改定时发布 |
| DELETE | `/api/owner/posts/{postId}/schedule` | OWNER + `POST_EDIT` | `postId:string`, Query `version` | 无 | `PostSummaryVO` | 200, 400, 401, 403, 404, 409, 500 | 同上 | 取消定时发布并回到 DRAFT |
| PUT | `/api/owner/posts/{postId}/autosave` | OWNER + `POST_EDIT` | `postId:string` | `AutosavePostDTO` | `AutosaveVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | 编辑器自动保存 |
| GET | `/api/owner/posts/{postId}/versions` | OWNER + `POST_EDIT` | `postId:string`, `page`, `pageSize` | 无 | `PageVO<PostVersionVO>` | 200, 400, 401, 403, 404, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 历史版本列表 |
| GET | `/api/owner/posts/{postId}/versions/{versionId}` | OWNER + `POST_EDIT` | 两个 ID 均为 string | 无 | `PostVersionDetailVO` | 200, 401, 403, 404, 500 | `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `INTERNAL_ERROR` | 查看历史正文 |
| POST | `/api/owner/posts/{postId}/versions/{versionId}/restore` | OWNER + `POST_EDIT` | 两个 ID 均为 string | `RestoreVersionDTO` | `PostSummaryVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `INTERNAL_ERROR` | 恢复历史版本 |
| GET | `/api/owner/posts/trash` | OWNER + `POST_DELETE` | `type=ALL|TECH|MOMENT`, `page`, `pageSize` | 无 | `PageVO<PostSummaryVO>` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 回收站 |
| POST | `/api/owner/posts/{postId}/restore` | OWNER + `POST_DELETE` | `postId:string`, Query `version` | 无 | `PostSummaryVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 从回收站恢复为 DRAFT |
| POST | `/api/owner/posts/batch/publish` | OWNER + `POST_EDIT` | 无 | `BatchPostDTO` | `BatchPostResultVO` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 批量发布 |
| POST | `/api/owner/posts/batch/delete` | OWNER + `POST_DELETE` | 无 | `BatchPostDTO` | `BatchPostResultVO` | 200, 400, 401, 403, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `INTERNAL_ERROR` | 批量软删除 |
| PUT | `/api/owner/posts/tech/{id}/slug` | OWNER + `POST_EDIT` | `id:string` | `UpdateSlugDTO` | `TechDetailVO` | 200, 400, 401, 403, 404, 409, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VERSION_CONFLICT`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR` | 手动修改 TECH slug |
| POST | `/api/owner/posts/import/markdown` | OWNER + `POST_CREATE` | 无 | multipart `file` | `MarkdownImportVO` | 200, 400, 401, 403, 413, 415, 500 | `BAD_REQUEST`, `AUTH_REQUIRED`, `FORBIDDEN`, `PAYLOAD_TOO_LARGE`, `UNSUPPORTED_MEDIA_TYPE`, `INTERNAL_ERROR` | 解析 Markdown 供编辑器预览，不直接发布 |

定时任务仅发布 `status=SCHEDULED AND publish_at<=now` 且未删除内容。历史版本至少在正式保存、发布、恢复之前产生快照。批量接口单项失败不回滚已成功项，必须逐项返回结果；前端据此刷新失败项。

## 23. 下一阶段数据库迁移边界与实施顺序

本文件不修改现有 SQL，但 2～8 不能只靠 Controller 完成。正式编码前应另建增量迁移，至少覆盖：

- `space_user` 支持 VISITOR；Visitor 展示资料字段。
- Post Like 关系及唯一约束。
- 通用媒体资产及引用检查。
- Notification 持久化。
- Post autosave、history/version、schedule 所需字段或表。
- Elasticsearch 索引与重建任务状态（任务状态可落 MySQL 或可靠任务系统）。

建议开发顺序：Visitor 注册登录 → Visitor 评论/留言 → 点赞 → 媒体上传 → Redis 业务缓存 → SSE 通知 → 搜索/推荐 → 内容高级能力。每一阶段先完成数据库迁移和后端测试，再接前端；不要一次性把所有模块混在一个提交中。
