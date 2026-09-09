package com.byy.blogprojectbackend.site.enums;

/** site_config 的配置记录标识。V1 只有一份主配置。 */
public enum SiteConfigKey {
    PRIMARY("主空间配置");

    private final String label;

    SiteConfigKey(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }
}
