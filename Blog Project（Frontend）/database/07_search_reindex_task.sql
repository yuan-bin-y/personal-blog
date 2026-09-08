-- BinSpace V2 / Feature 7: Elasticsearch reindex task persistence.

USE `binspace`;

CREATE TABLE IF NOT EXISTS `search_reindex_task` (
  `task_id` VARCHAR(32) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `started_at` DATETIME(3) NULL,
  `finished_at` DATETIME(3) NULL,
  `indexed_count` INT NOT NULL DEFAULT 0,
  `failure_message` VARCHAR(500) NULL,
  `active_marker` TINYINT
    GENERATED ALWAYS AS (
      CASE WHEN `status` IN ('QUEUED', 'RUNNING') THEN 1 ELSE NULL END
    ) STORED,
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `uk_search_reindex_single_active` (`active_marker`),
  KEY `idx_search_reindex_created_at` (`created_at` DESC),
  KEY `idx_search_reindex_created_by` (`created_by`, `created_at` DESC),
  CONSTRAINT `fk_search_reindex_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `ck_search_reindex_status`
    CHECK (`status` IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED')),
  CONSTRAINT `ck_search_reindex_indexed_count`
    CHECK (`indexed_count` >= 0)
) ENGINE=InnoDB;
