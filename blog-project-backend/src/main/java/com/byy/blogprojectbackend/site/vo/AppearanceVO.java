package com.byy.blogprojectbackend.site.vo;

public record AppearanceVO(
        String layoutMode,
        String backgroundMode,
        double surfaceOpacity,
        double backdropShade,
        WallpaperVO wallpaper,
        boolean allowVisitorControls
) {

    public record WallpaperVO(
            String desktop,
            String mobile
    ) {
    }
}