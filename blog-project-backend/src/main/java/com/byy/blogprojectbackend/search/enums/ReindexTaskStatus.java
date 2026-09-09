package com.byy.blogprojectbackend.search.enums;

/** Elasticsearch 重建索引任务状态。 */
public enum ReindexTaskStatus {
    QUEUED("等待执行"),
    RUNNING("执行中"),
    SUCCEEDED("执行成功"),
    FAILED("执行失败");

    private final String label;

    ReindexTaskStatus(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
