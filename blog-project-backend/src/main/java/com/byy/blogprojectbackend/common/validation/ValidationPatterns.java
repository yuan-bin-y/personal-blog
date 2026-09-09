package com.byy.blogprojectbackend.common.validation;

/** Bean Validation 共用的稳定格式规则。 */
public final class ValidationPatterns {

    public static final String POSITIVE_INTEGER = "[1-9]\\d*";
    public static final String SLUG = "[a-z0-9]+(?:-[a-z0-9]+)*";
    public static final String REINDEX_TASK_ID = "[a-f0-9]{32}";

    private ValidationPatterns() {
    }
}
