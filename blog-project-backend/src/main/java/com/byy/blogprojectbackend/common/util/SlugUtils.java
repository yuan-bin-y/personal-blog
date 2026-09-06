package com.byy.blogprojectbackend.common.util;

import java.text.Normalizer;
import java.util.Locale;

/** URL slug 的统一规范化工具。 */
public final class SlugUtils {

    private SlugUtils() {
    }

    public static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        String slug = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");

        return slug.isBlank() ? fallback : slug;
    }
}
