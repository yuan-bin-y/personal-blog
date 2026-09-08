# BinSpace Database

MySQL 8.x database scripts generated from the frozen `docs/DATABASE_DESIGN.md` and `docs/API_DESIGN.md`.

## Files

- `01_schema.sql`：创建 `binspace` 数据库、表、外键、检查约束、唯一约束和索引。
- `02_seed.sql`：写入当前前端已经存在的站点配置。
- `03_visitor_accounts.sql`：允许 `space_user.role=VISITOR`，用于 Visitor 注册。
- `04_post_like.sql`：新增登录用户的 Post 点赞关系表。
- `05_media_asset.sql`：新增 Owner 媒体资产表及上传/删除恢复状态。
- `06_notification.sql`：新增通知持久化表，供通知中心和 SSE 断线补偿使用。
- `07_search_reindex_task.sql`：新增 Elasticsearch 索引重建任务表，持久化异步任务进度与失败原因。
- `08_owner_advanced_posts.sql`：新增定时发布状态、编辑器自动保存和内容历史版本。

## Execution

按顺序执行：

```bash
mysql -u root -p < 01_schema.sql
mysql -u root -p < 02_seed.sql
mysql -u root -p < 03_visitor_accounts.sql
mysql -u root -p < 04_post_like.sql
mysql -u root -p < 05_media_asset.sql
mysql -u root -p < 06_notification.sql
mysql -u root -p < 07_search_reindex_task.sql
mysql -u root -p < 08_owner_advanced_posts.sql
```

也可以在 MySQL 客户端中按编号依次 `SOURCE` 八个文件。

要求：

- MySQL 8.0.16+，确保 CHECK 约束会被执行。
- 数据库字符集为 `utf8mb4`。
- 执行账号需要 CREATE DATABASE、CREATE TABLE、ALTER/REFERENCES 和 INSERT 权限。

## Seed policy

`02_seed.sql` 只写入来自当前 BinSpace 前端的真实基础配置：

- 空间名称与简介
- Hero 文案和媒体路径
- 外观与各页面背景配置
- 空外链配置

它不会插入：

- 伪造的 TECH/MOMENT
- 伪造的评论或留言
- 虚构分类和标签
- 带默认弱密码的 OWNER

`site_config.updated_by` 初始为 NULL，表示数据库初始化产生的系统配置。OWNER 后续通过 API 修改配置后，后端应写入真实 OWNER ID。

## OWNER provisioning

数据库脚本故意不提供默认 OWNER 密码。后端实现时应通过一次性安全初始化流程创建：

1. `space_user`：密码必须先由 Spring Security `PasswordEncoder` 生成强哈希后再保存。
2. 对应的 `profile`：`user_id` 必须引用该 OWNER。

不要把明文密码、通用默认密码或临时 BCrypt 字符串提交到 Seed 文件。

## ID policy

- 表主键使用 BIGINT，但不使用 AUTO_INCREMENT。
- 后端建议使用 MyBatis-Plus `ASSIGN_ID` 或统一的服务端雪花 ID。
- 所有 BIGINT ID 进入 JSON 时必须序列化为 string，避免 JavaScript 精度损失。
- Seed 中 `site_config.id=1` 是固定系统单例，不参与业务 ID 生成。

## Authentication storage

当前后端使用 Spring Security + Bearer JWT，并由 Redis 保存 Token 的有效登录状态；退出登录时撤销对应 Redis Token Session。MySQL 不保存 access token 或 refresh token。

## Content media boundary

- TECH 正文图片 URL 直接保存在 Markdown `post.content` 中。
- TECH Cover 写入 `post_media`，`usage_type=COVER`。
- MOMENT 图片写入 `post_media`，`usage_type=CONTENT`。
- `media_asset` 保存 Owner 上传媒体的存储定位、文件元数据和一致性恢复状态；业务表继续只保存最终 URL。

## Re-running

- `01_schema.sql` / `02_seed.sql` 使用 `IF NOT EXISTS` / `INSERT IGNORE`；`03`～`08` 是已有数据库的增量迁移，应按编号记录执行情况。
- 这些文件不是迁移框架。已有表发生结构变更后，应新增版本化迁移脚本，不要依赖重新运行 `01_schema.sql` 修改旧表。
- 正式执行前仍建议备份目标数据库。
