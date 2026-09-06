# BinSpace Owner 业务动作清单

本文件只记录 Owner 前端原型验证出的业务动作与数据需求，不设计 REST 路径、数据库表、鉴权方案或存储实现。

当前前端的所有修改仅保存在页面运行时内存中，刷新即恢复。未来接入后端时，所有写操作都必须由服务端校验真实 OWNER 身份，不能信任前端的 `isOwner`。

## 身份与空间初始化

### Read Current Identity

- 用途：确认当前访问者是否为 OWNER。
- 需要：身份标识、角色、可用权限。

### Read Space Configuration

- 用途：一次性读取空间基本信息、Profile、Hero、公告与音乐配置。
- 需要：各配置当前值、更新时间。

## TECH

### Create Tech Post

- 需要：`title`、`summary`、`category`、`tags`、`cover`、`content`。
- 服务端生成：`id`、`slug`、`createdAt`、默认互动计数；阅读时间可由服务端计算或接受明确规则后的前端值。

### Edit Tech Post

- 需要：目标文章标识及可修改的 `title`、`summary`、`category`、`tags`、`cover`、`content`。
- 需要明确：修改标题时 slug 是否保持稳定。

### Delete Tech Post

- 需要：目标文章标识、删除确认。
- 需要明确：软删除/彻底删除策略，以及关联评论和媒体的处理方式。

## MOMENT

### Create Moment

- 需要：`content`、`images`。
- 服务端生成：`id`、`createdAt`、默认互动计数。
- MOMENT 不需要也不应自动生成 `title`。

### Edit Moment

- 需要：目标说说标识、`content`、`images`。

### Delete Moment

- 需要：目标说说标识、删除确认。
- 需要明确：关联评论和媒体的处理方式。

## Profile

### Update Profile

- 需要：`name`、`avatar`、`role`、`bio`、`status`。
- 当前前端只填写媒体路径；未来头像文件由独立媒体上传能力提供地址。

## Hero

### Update Hero

- 需要：`desktopVideo`、`mobileVideo`、`poster`、`heroTitle`、`heroSubtitle`。
- 媒体上传与 OSS 管理属于独立能力，本动作只保存已经获得的媒体地址和展示文案。

## Announcement

### Update Announcement

- 需要：公告正文。
- 需要明确：空正文代表隐藏公告还是展示空状态。

## Music

### Update Music

- 需要：`title`、`artist`、`audioUrl`、`cover`。
- 音频上传不属于本动作；这里只保存媒体地址与展示信息。

## Space Basic Info

### Update Space Basic Info

- 需要：空间英文名、中文名、一句话说明。

## Comment（属于具体 Post）

### Reply Comment

- 需要：评论标识、回复正文。
- 服务端生成：回复时间与 OWNER 身份信息。

### Delete Comment

- 需要：评论标识、删除确认。
- 需要明确：删除后是否保留“已删除”占位。

## Guestbook（属于整个 Space）

### Reply Guestbook

- 需要：空间留言标识、回复正文。
- 服务端生成：回复时间与 OWNER 身份信息。

### Delete Guestbook

- 需要：空间留言标识、删除确认。
- Guestbook 与 Comment 必须保持独立业务边界，不能用 Post 评论接口代替。

## 共通返回与错误需求

- 写操作成功后返回最新实体或明确的新版本号，便于页面立即同步。
- 返回字段级校验错误，供编辑表单定位问题。
- 返回无权限、资源不存在、资源已被修改等可区分状态。
- 媒体地址需要包含前端实际展示所需的可访问 URL；上传流程后续单独设计。
