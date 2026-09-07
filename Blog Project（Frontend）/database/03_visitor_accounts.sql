-- BinSpace V2 / Feature 2: allow registered Visitor accounts.
-- Execute once against the existing binspace database before testing registration.

USE `binspace`;

ALTER TABLE `space_user`
  DROP CHECK `ck_space_user_role`;

ALTER TABLE `space_user`
  MODIFY COLUMN `role` VARCHAR(16) NOT NULL DEFAULT 'VISITOR';

ALTER TABLE `space_user`
  ADD CONSTRAINT `ck_space_user_role`
  CHECK (`role` IN ('OWNER', 'VISITOR'));
