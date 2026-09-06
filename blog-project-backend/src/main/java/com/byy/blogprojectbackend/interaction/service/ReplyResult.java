package com.byy.blogprojectbackend.interaction.service;

/** created=true 表示新建回复（HTTP 201），false 表示恢复旧回复（HTTP 200）。 */
public record ReplyResult<T>(T value,boolean created) {
}
