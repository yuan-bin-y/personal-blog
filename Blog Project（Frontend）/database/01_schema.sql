-- BinSpace V1 schema
-- Target: MySQL 8.x
-- This script creates the database and tables only. It does not create application users.

CREATE DATABASE IF NOT EXISTS `binspace`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `binspace`;

CREATE TABLE IF NOT EXISTS `space_user` (
  `id` BIGINT NOT NULL,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `role` VARCHAR(16) NOT NULL DEFAULT 'OWNER',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  `last_login_at` DATETIME(3) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` INT UNSIGNED NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_space_user_username` (`username`),
  CONSTRAINT `ck_space_user_role` CHECK (`role` IN ('OWNER')),
  CONSTRAINT `ck_space_user_status` CHECK (`status` IN ('ACTIVE', 'DISABLED', 'LOCKED')),
  CONSTRAINT `ck_space_user_deleted` CHECK (`deleted` IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `profile` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `display_name` VARCHAR(64) NOT NULL,
  `avatar_url` VARCHAR(1024) NULL,
  `role_text` VARCHAR(128) NULL,
  `bio` VARCHAR(500) NULL,
  `status_label` VARCHAR(64) NULL,
  `status_text` VARCHAR(255) NULL,
  `status_emoji` VARCHAR(32) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_user_id` (`user_id`),
  CONSTRAINT `fk_profile_user`
    FOREIGN KEY (`user_id`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `slug` VARCHAR(80) NOT NULL,
  `description` VARCHAR(255) NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` INT UNSIGNED NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`),
  UNIQUE KEY `uk_category_slug` (`slug`),
  KEY `idx_category_visible` (`deleted`, `sort_order`, `id`),
  CONSTRAINT `ck_category_deleted` CHECK (`deleted` IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `slug` VARCHAR(80) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`name`),
  UNIQUE KEY `uk_tag_slug` (`slug`),
  KEY `idx_tag_visible` (`deleted`, `name`),
  CONSTRAINT `ck_tag_deleted` CHECK (`deleted` IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `post` (
  `id` BIGINT NOT NULL,
  `type` VARCHAR(16) NOT NULL,
  `slug` VARCHAR(180) NULL,
  `title` VARCHAR(180) NULL,
  `summary` VARCHAR(600) NULL,
  `content` LONGTEXT NOT NULL,
  `content_format` VARCHAR(16) NOT NULL DEFAULT 'PLAIN_TEXT',
  `category_id` BIGINT NULL,
  `reading_time_minutes` SMALLINT UNSIGNED NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  `published_at` DATETIME(3) NULL,
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `comment_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT NOT NULL,
  `updated_by` BIGINT NOT NULL,
  `version` INT UNSIGNED NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  `deleted_by` BIGINT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_slug` (`slug`),
  KEY `idx_post_public_feed` (`deleted`, `status`, `published_at` DESC, `id` DESC),
  KEY `idx_post_type_feed` (`type`, `deleted`, `status`, `published_at` DESC, `id` DESC),
  KEY `idx_post_category_feed` (`category_id`, `deleted`, `status`, `published_at` DESC),
  KEY `idx_post_created_by` (`created_by`, `created_at` DESC),
  KEY `idx_post_updated_by` (`updated_by`),
  KEY `idx_post_deleted_by` (`deleted_by`),
  CONSTRAINT `fk_post_category`
    FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_post_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_post_updated_by`
    FOREIGN KEY (`updated_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_post_deleted_by`
    FOREIGN KEY (`deleted_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `ck_post_type` CHECK (`type` IN ('TECH', 'MOMENT')),
  CONSTRAINT `ck_post_content_format` CHECK (`content_format` IN ('MARKDOWN', 'PLAIN_TEXT')),
  CONSTRAINT `ck_post_status` CHECK (`status` IN ('DRAFT', 'PUBLISHED')),
  CONSTRAINT `ck_post_deleted` CHECK (`deleted` IN (0, 1)),
  CONSTRAINT `ck_post_publish_time` CHECK (
    (`status` = 'DRAFT' AND `published_at` IS NULL)
    OR (`status` = 'PUBLISHED' AND `published_at` IS NOT NULL)
  ),
  CONSTRAINT `ck_post_type_fields` CHECK (
    (
      `type` = 'TECH'
      AND `slug` IS NOT NULL
      AND `title` IS NOT NULL
      AND `summary` IS NOT NULL
      AND `category_id` IS NOT NULL
      AND `reading_time_minutes` IS NOT NULL
      AND `reading_time_minutes` > 0
    )
    OR
    (
      `type` = 'MOMENT'
      AND `slug` IS NULL
      AND `title` IS NULL
      AND `summary` IS NULL
      AND `category_id` IS NULL
      AND `reading_time_minutes` IS NULL
    )
  )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `post_tag` (
  `post_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`post_id`, `tag_id`),
  KEY `idx_post_tag_tag_post` (`tag_id`, `post_id`),
  CONSTRAINT `fk_post_tag_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `fk_post_tag_tag`
    FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `post_media` (
  `id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `usage_type` VARCHAR(16) NOT NULL DEFAULT 'CONTENT',
  `media_type` VARCHAR(16) NOT NULL DEFAULT 'IMAGE',
  `url` VARCHAR(1024) NOT NULL,
  `poster_url` VARCHAR(1024) NULL,
  `alt_text` VARCHAR(255) NULL,
  `width` INT UNSIGNED NULL,
  `height` INT UNSIGNED NULL,
  `sort_order` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_media_post_order` (`post_id`, `deleted`, `sort_order`, `id`),
  CONSTRAINT `fk_post_media_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `ck_post_media_usage` CHECK (`usage_type` IN ('COVER', 'CONTENT')),
  CONSTRAINT `ck_post_media_type` CHECK (`media_type` IN ('IMAGE', 'VIDEO')),
  CONSTRAINT `ck_post_media_deleted` CHECK (`deleted` IN (0, 1)),
  CONSTRAINT `ck_post_media_dimensions` CHECK (
    (`width` IS NULL OR `width` > 0) AND (`height` IS NULL OR `height` > 0)
  )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `parent_id` BIGINT NULL,
  `author_user_id` BIGINT NULL,
  `author_name` VARCHAR(64) NOT NULL,
  `author_avatar_url` VARCHAR(1024) NULL,
  `content` VARCHAR(2000) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  `deleted_by` BIGINT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_parent` (`parent_id`),
  KEY `idx_comment_post_root` (`post_id`, `deleted`, `parent_id`, `created_at`, `id`),
  KEY `idx_comment_author_user` (`author_user_id`),
  KEY `idx_comment_deleted_by` (`deleted_by`),
  CONSTRAINT `fk_comment_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `fk_comment_author_user`
    FOREIGN KEY (`author_user_id`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `fk_comment_deleted_by`
    FOREIGN KEY (`deleted_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `ck_comment_deleted` CHECK (`deleted` IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `guestbook` (
  `id` BIGINT NOT NULL,
  `parent_id` BIGINT NULL,
  `author_user_id` BIGINT NULL,
  `author_name` VARCHAR(64) NOT NULL,
  `author_avatar_url` VARCHAR(1024) NULL,
  `content` VARCHAR(2000) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  `deleted_by` BIGINT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_guestbook_parent` (`parent_id`),
  KEY `idx_guestbook_root` (`deleted`, `parent_id`, `created_at` DESC, `id` DESC),
  KEY `idx_guestbook_author_user` (`author_user_id`),
  KEY `idx_guestbook_deleted_by` (`deleted_by`),
  CONSTRAINT `fk_guestbook_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `guestbook` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `fk_guestbook_author_user`
    FOREIGN KEY (`author_user_id`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `fk_guestbook_deleted_by`
    FOREIGN KEY (`deleted_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `ck_guestbook_deleted` CHECK (`deleted` IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `site_config` (
  `id` BIGINT NOT NULL,
  `singleton_key` VARCHAR(32) NOT NULL DEFAULT 'PRIMARY',
  `site_name` VARCHAR(64) NOT NULL,
  `site_chinese_name` VARCHAR(64) NOT NULL,
  `site_description` VARCHAR(255) NULL,
  `hero_eyebrow` VARCHAR(128) NULL,
  `hero_title` VARCHAR(180) NOT NULL,
  `hero_subtitle` VARCHAR(500) NULL,
  `hero_desktop_video_url` VARCHAR(1024) NULL,
  `hero_mobile_video_url` VARCHAR(1024) NULL,
  `hero_poster_url` VARCHAR(1024) NULL,
  `announcement_content` VARCHAR(2000) NULL,
  `announcement_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `music_title` VARCHAR(180) NULL,
  `music_artist` VARCHAR(180) NULL,
  `music_audio_url` VARCHAR(1024) NULL,
  `music_cover_url` VARCHAR(1024) NULL,
  `appearance_json` JSON NULL,
  `page_media_json` JSON NULL,
  `external_links_json` JSON NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `updated_by` BIGINT NULL,
  `version` INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_site_config_singleton` (`singleton_key`),
  KEY `idx_site_config_updated_by` (`updated_by`),
  CONSTRAINT `fk_site_config_updated_by`
    FOREIGN KEY (`updated_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `ck_site_config_singleton` CHECK (`singleton_key` = 'PRIMARY'),
  CONSTRAINT `ck_site_config_announcement_enabled` CHECK (`announcement_enabled` IN (0, 1)),
  CONSTRAINT `ck_site_config_announcement` CHECK (
    `announcement_enabled` = 0
    OR (`announcement_content` IS NOT NULL AND CHAR_LENGTH(TRIM(`announcement_content`)) > 0)
  )
) ENGINE=InnoDB;

