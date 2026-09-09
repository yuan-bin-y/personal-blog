package com.byy.blogprojectbackend.media.enums;

/** 媒体文件的实际存储位置；code 用于配置文件和数据库，label 用于中文展示。 */
public enum MediaStorageProvider {
    LOCAL("local", "本地存储"),
    ALIYUN_OSS("aliyun-oss", "阿里云 OSS");

    public static final String LOCAL_CODE = "local";
    public static final String ALIYUN_OSS_CODE = "aliyun-oss";

    private final String code;
    private final String label;

    MediaStorageProvider(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }
}
