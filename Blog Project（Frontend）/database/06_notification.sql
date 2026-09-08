-- BinSpace V2 / Feature 5: persistent notifications for SSE realtime delivery.

USE `binspace`;

CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL,
  `recipient_user_id` BIGINT NOT NULL,
  `actor_user_id` BIGINT NULL,
  `type` VARCHAR(32) NOT NULL,
  `resource_type` VARCHAR(16) NOT NULL,
  `resource_id` BIGINT NOT NULL,
  `summary` VARCHAR(200) NOT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `read_at` DATETIME(3) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_notification_recipient_time`
    (`recipient_user_id`, `created_at` DESC, `id` DESC),
  KEY `idx_notification_recipient_unread`
    (`recipient_user_id`, `is_read`, `created_at` DESC, `id` DESC),
  KEY `idx_notification_actor` (`actor_user_id`, `created_at` DESC),
  CONSTRAINT `fk_notification_recipient`
    FOREIGN KEY (`recipient_user_id`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_notification_actor`
    FOREIGN KEY (`actor_user_id`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT `ck_notification_type`
    CHECK (`type` IN (
      'COMMENT_CREATED',
      'GUESTBOOK_CREATED',
      'COMMENT_REPLIED',
      'GUESTBOOK_REPLIED'
    )),
  CONSTRAINT `ck_notification_resource_type`
    CHECK (`resource_type` IN ('POST', 'COMMENT', 'GUESTBOOK')),
  CONSTRAINT `ck_notification_is_read`
    CHECK (`is_read` IN (0, 1))
) ENGINE=InnoDB;
