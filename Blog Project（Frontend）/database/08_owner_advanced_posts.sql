-- BinSpace V2 / Feature 8: Owner advanced post capabilities.

USE `binspace`;

-- SCHEDULED uses published_at as the planned publication time.
ALTER TABLE `post`
  DROP CHECK `ck_post_status`,
  DROP CHECK `ck_post_publish_time`,
  ADD CONSTRAINT `ck_post_status`
    CHECK (`status` IN ('DRAFT', 'SCHEDULED', 'PUBLISHED')),
  ADD CONSTRAINT `ck_post_publish_time`
    CHECK (
      (`status` = 'DRAFT' AND `published_at` IS NULL)
      OR (`status` IN ('SCHEDULED', 'PUBLISHED') AND `published_at` IS NOT NULL)
    ),
  ADD KEY `idx_post_scheduled_publish`
    (`status`, `deleted`, `published_at`, `id`);

-- One current autosave per Post. It is deliberately separate from the formal Post.
CREATE TABLE IF NOT EXISTS `post_autosave` (
  `id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `post_type` VARCHAR(16) NOT NULL,
  `payload_json` JSON NOT NULL,
  `base_version` INT UNSIGNED NOT NULL,
  `saved_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_by` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_autosave_post` (`post_id`),
  KEY `idx_post_autosave_updated_by` (`updated_by`, `saved_at` DESC),
  CONSTRAINT `fk_post_autosave_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `fk_post_autosave_updated_by`
    FOREIGN KEY (`updated_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `ck_post_autosave_type`
    CHECK (`post_type` IN ('TECH', 'MOMENT'))
) ENGINE=InnoDB;

-- Immutable snapshots created before formal content/state changes.
CREATE TABLE IF NOT EXISTS `post_version` (
  `id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `version_no` INT UNSIGNED NOT NULL,
  `post_type` VARCHAR(16) NOT NULL,
  `summary` VARCHAR(200) NOT NULL,
  `snapshot_json` JSON NOT NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_version_number` (`post_id`, `version_no`),
  KEY `idx_post_version_list` (`post_id`, `created_at` DESC, `id` DESC),
  KEY `idx_post_version_created_by` (`created_by`, `created_at` DESC),
  CONSTRAINT `fk_post_version_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT `fk_post_version_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `ck_post_version_type`
    CHECK (`post_type` IN ('TECH', 'MOMENT')),
  CONSTRAINT `ck_post_version_number`
    CHECK (`version_no` > 0)
) ENGINE=InnoDB;
