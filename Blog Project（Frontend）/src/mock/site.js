export const navigationItems = [
  { label: '首页', to: '/', available: true },
  { label: '说说', to: '/moments', available: true },
  { label: '技术', to: '/tech', available: true },
  { label: '留言板', to: '/guestbook', available: true },
  { label: '归档', to: '/archive', available: true },
  { label: '关于', to: '/about', available: true },
]

export const heroContent = {
  eyebrow: 'BINSPACE · PERSONAL DAYBOOK',
  title: '在暖光里，留下代码与生活。',
  description: '这里收录认真写下的技术文章，也收藏那些不必宏大的日常时刻。',
  media: {
    desktop: { webm: '', mp4: '/media/binspace-hero.mp4' },
    mobile: { webm: '', mp4: '/media/binspace-hero.mp4' },
    poster: { avif: '', fallback: '' },
  },
}

// 当前由前端配置提供；后续后台只需返回同结构的媒体 URL 与显示参数。
export const appearanceConfig = {
  layoutMode: 'standard',
  backgroundMode: 'wallpaper',
  surfaceOpacity: 0.54,
  backdropShade: 0.3,
  wallpaper: {
    desktop: '/media/pages/home-beyond-mountain.jpg',
    mobile: '/media/pages/home-beyond-mountain.jpg',
  },
  allowVisitorControls: true,
}

export const musicMedia = {
  title: '',
  artist: '',
  cover: '',
  src: '',
}

export const pageMedia = {
  moments: {
    video: '',
    poster: '/media/pages/moments-mountain-hd.png',
  },
  guestbook: {
    video: '/media/pages/guestbook-whale.mp4',
    poster: '/media/pages/guestbook-whale.jpg',
  },
  tech: {
    video: '',
    poster: '/media/pages/tech-quiet-time.jpg',
    motion: 'drift',
    effect: 'petals',
  },
  archive: {
    video: '/media/pages/archive-horizon-sky.mp4',
    poster: '/media/pages/archive-horizon-sky.jpg',
  },
  about: {
    video: '',
    poster: '/media/pages/about-last-train.jpg',
    overlay: '/media/pages/about-last-train-layer.png',
    motion: 'train',
  },
}

export const siteMeta = {
  name: 'BinSpace',
  chineseName: '玢的空间',
  description: '记录代码，也记录生活。',
  currentYear: 2026,
}

export const spaceWidgets = {
  note: '等待后端开发',
  currently: '等待后端开发',
  tags: [],
  archives: [],
}

export const externalLinks = {
  homepage: '',
  github: '',
}
