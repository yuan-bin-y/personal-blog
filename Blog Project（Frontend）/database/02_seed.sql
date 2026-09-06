-- BinSpace V1 minimal seed
-- Inserts only the existing frontend site configuration.
-- No fake posts, comments, guestbook entries, categories, tags, or default-password owner account.

USE `binspace`;

INSERT IGNORE INTO `site_config` (
  `id`,
  `singleton_key`,
  `site_name`,
  `site_chinese_name`,
  `site_description`,
  `hero_eyebrow`,
  `hero_title`,
  `hero_subtitle`,
  `hero_desktop_video_url`,
  `hero_mobile_video_url`,
  `hero_poster_url`,
  `announcement_content`,
  `announcement_enabled`,
  `music_title`,
  `music_artist`,
  `music_audio_url`,
  `music_cover_url`,
  `appearance_json`,
  `page_media_json`,
  `external_links_json`,
  `updated_by`,
  `version`
) VALUES (
  1,
  'PRIMARY',
  'BinSpace',
  '玢的空间',
  '记录代码，也记录生活。',
  'BINSPACE · PERSONAL DAYBOOK',
  '在暖光里，留下代码与生活。',
  '这里收录认真写下的技术文章，也收藏那些不必宏大的日常时刻。',
  '/media/binspace-hero.mp4',
  '/media/binspace-hero.mp4',
  NULL,
  NULL,
  0,
  NULL,
  NULL,
  NULL,
  NULL,
  JSON_OBJECT(
    'layoutMode', 'standard',
    'backgroundMode', 'wallpaper',
    'surfaceOpacity', 0.54,
    'backdropShade', 0.30,
    'wallpaper', JSON_OBJECT(
      'desktop', '/media/pages/home-beyond-mountain.jpg',
      'mobile', '/media/pages/home-beyond-mountain.jpg'
    ),
    'allowVisitorControls', TRUE
  ),
  JSON_OBJECT(
    'moments', JSON_OBJECT(
      'video', '',
      'poster', '/media/pages/moments-mountain-hd.png'
    ),
    'guestbook', JSON_OBJECT(
      'video', '/media/pages/guestbook-whale.mp4',
      'poster', '/media/pages/guestbook-whale.jpg'
    ),
    'tech', JSON_OBJECT(
      'video', '',
      'poster', '/media/pages/tech-quiet-time.jpg',
      'motion', 'drift',
      'effect', 'petals'
    ),
    'archive', JSON_OBJECT(
      'video', '/media/pages/archive-horizon-sky.mp4',
      'poster', '/media/pages/archive-horizon-sky.jpg'
    ),
    'about', JSON_OBJECT(
      'video', '',
      'poster', '/media/pages/about-last-train.jpg',
      'overlay', '/media/pages/about-last-train-layer.png',
      'motion', 'train'
    )
  ),
  JSON_OBJECT(
    'homepage', '',
    'github', ''
  ),
  NULL,
  0
);

