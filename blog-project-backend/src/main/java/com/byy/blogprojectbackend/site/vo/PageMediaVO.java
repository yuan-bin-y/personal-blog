package com.byy.blogprojectbackend.site.vo;

public record PageMediaVO(
        PageMediaItemVO moments,
        PageMediaItemVO guestbook,
        PageMediaItemVO tech,
        PageMediaItemVO archive,
        PageMediaItemVO about
) {

    public record PageMediaItemVO(
            String video,
            String poster,
            String overlay,
            String motion,
            String effect
    ) {
    }
}