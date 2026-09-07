-- BinSpace V2 / Feature 4: managed media assets for local storage and OSS.

USE `binspace`;

CREATE TABLE IF NOT EXISTS `media_asset` (
  `id` BIGINT NOT NULL,
  `media_type` VARCHAR(16) NOT NULL,
  `usage_type` VARCHAR(32) NOT NULL,
  `storage_provider` VARCHAR(24) NOT NULL,
  `bucket_name` VARCHAR(128) NULL,
  `object_key` VARCHAR(512) NOT NULL,
  `url` VARCHAR(1024) NULL,
  `original_name` VARCHAR(255) NOT NULL,
  `content_type` VARCHAR(128) NOT NULL,
  `size_bytes` BIGINT UNSIGNED NOT NULL,
  `width` INT UNSIGNED NULL,
  `height` INT UNSIGNED NULL,
  `duration_seconds` DECIMAL(12,3) NULL,
  `status` VARCHAR(24) NOT NULL DEFAULT 'UPLOADING',
  `retry_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `last_error` VARCHAR(1000) NULL,
  `next_retry_at` DATETIME(3) NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) NULL,
  `deleted_by` BIGINT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_media_asset_object_key` (`object_key`),
  KEY `idx_media_asset_library` (`deleted`, `status`, `media_type`, `usage_type`, `created_at` DESC, `id` DESC),
  KEY `idx_media_asset_retry` (`status`, `next_retry_at`, `retry_count`),
  KEY `idx_media_asset_created_by` (`created_by`, `created_at` DESC),
  CONSTRAINT `fk_media_asset_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_media_asset_deleted_by`
    FOREIGN KEY (`deleted_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `ck_media_asset_type`
    CHECK (`media_type` IN ('IMAGE', 'VIDEO', 'AUDIO')),
  CONSTRAINT `ck_media_asset_usage`
    CHECK (`usage_type` IN ('AVATAR', 'HERO', 'POST_COVER', 'POST_CONTENT', 'MUSIC', 'PAGE_BACKGROUND')),
  CONSTRAINT `ck_media_asset_status`
    CHECK (`status` IN ('UPLOADING', 'ACTIVE', 'UPLOAD_FAILED', 'DELETING', 'DELETE_FAILED', 'DELETED')),
  CONSTRAINT `ck_media_asset_deleted`
    CHECK (`deleted` IN (0, 1)),
  CONSTRAINT `ck_media_asset_dimensions`
    CHECK ((`width` IS NULL OR `width` > 0) AND (`height` IS NULL OR `height` > 0)),
  CONSTRAINT `ck_media_asset_duration`
    CHECK (`duration_seconds` IS NULL OR `duration_seconds` >= 0)
) ENGINE=InnoDB;
