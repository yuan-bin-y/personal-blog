-- BinSpace V2 / Feature 3: persistent Post likes.
-- Execute once against the existing binspace database before testing Likes.

USE `binspace`;

CREATE TABLE IF NOT EXISTS `post_like` (
  `id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_like_user_post` (`user_id`, `post_id`),
  KEY `idx_post_like_post` (`post_id`, `id`),
  KEY `idx_post_like_user_created` (`user_id`, `created_at` DESC, `id` DESC),
  CONSTRAINT `fk_post_like_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT `fk_post_like_user`
    FOREIGN KEY (`user_id`) REFERENCES `space_user` (`id`)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB;
