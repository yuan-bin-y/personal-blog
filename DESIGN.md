# BinSpace / 玢的空间 — Design System

> 适用范围：当前 Vue 前端，以及后续与内容展示有关的页面。当前阶段只使用 Mock Data；本文不定义后端、登录、API 或数据库方案。

## 1. Design Direction / 视觉方向

### 产品与用户

BinSpace 是一个以个人为中心的内容空间，而不是传统博客、后台系统或公共信息流产品。访客的主要任务依次是：感受空间主人的个性、浏览生活动态、阅读技术文章、对单条内容互动，以及在留言板给空间主人留言。

内容模型必须保持两类内容的差异：

- `MOMENT`：无强制标题的短内容，可带图片、时间、点赞与评论。
- `TECH`：有标题、摘要、分类、标签、阅读时间和正文入口的正式文章。

### 项目自有方向：暖日手账（Warm Daybook）

以奶油纸张般的明亮底色承载阅读，用珊瑚橙作为少量活跃信号，以深棕替代纯黑；大面积动态视频负责“动漫感”和情绪，正文区域则回到安静、清楚、略带手账气质的排版。界面应像被认真布置过的个人房间，而不是套用粉色素材的二次元模板。

### Reference Rationale / 参考依据

参考仅用于提取原则，不复制品牌构图、色值、字体、标识或专有资产。

| 参考 | 贡献层 | 提取的证据 | BinSpace 的原创转化 | 明确舍弃 |
|---|---|---|---|---|
| Claude（Design MD Advisor 策展索引） | 色彩氛围、编辑感 | 暖色、陶土色、人性化、平静的内容叙事 | 奶油白画布 + 自有珊瑚橙强调色 + 深棕文字；强调色只用于选中态和关键动作 | 品牌色值、产品营销构图、品牌字体 |
| Pinterest（同上） | 混合动态流、媒体 | 暖色背景、图片优先、混合比例媒体、弱化界面 chrome | Feed 允许 TECH 与 MOMENT 使用不同卡片结构；Moment 图片采用受控的 1/2/3 图拼版，不做无限瀑布流 | 品牌红、Pinterest 式保存按钮、完整瀑布流构图 |
| WIRED（同上） | 技术长文、信息层级 | 纸白、编辑排版、强标题与高密度归档能力 | 技术正文使用 68–72ch 阅读宽度、清晰元信息、目录和代码块；仍保留 BinSpace 的圆角与暖色体系 | 报刊品牌风格、自定义专有字体、过度密集的首页 |

未选用游戏/动漫官网作为视觉参考。动漫气质来自项目有权使用的 Hero 视频、插画切片和微动效，而不是霓虹、战斗 UI、角色 IP 或游戏官网式全屏控件。

### 成功标准

- 首屏先感知“个人空间”和生活气息，再看到内容筛选，不像 SaaS 落地页。
- `MOMENT` 与 `TECH` 在 2 秒内可通过结构识别，不能只靠标签颜色区分。
- 技术正文桌面端每行约 68–72 个拉丁字符，中文约 32–38 个全角字符。
- Hero 动态背景不会阻碍导航、标题、控制按钮阅读；静音自动播放失败时仍有完整 poster。
- 320px 宽度可无横向滚动使用；键盘、触摸和减少动态偏好均有等价体验。

## 2. Visual Atmosphere

- 明度：全站以浅色为默认；深色仅用于 Hero 遮罩、代码块和媒体观看面，不提供半成品全站暗色模式。
- 温度：背景偏奶油黄，辅助面偏杏色/淡珊瑚，避免大面积纯粉。
- 对比：正文使用深棕，辅助文字仍满足 WCAG AA；装饰线可以低对比，但不承担信息表达。
- 密度：首页为“松—紧—松”节奏：宽松 Hero、紧凑 Profile、舒适 Feed、再次放松的 Guestbook Preview。
- 几何：以 18–28px 中等圆角为主；导航和筛选可使用胶囊形，正文与图片避免所有元素都变成胶囊。
- 质感：使用 1px 暖灰描边和两级柔和阴影。禁止玻璃拟态堆叠、霓虹外发光和厚重 3D 边框。
- 个性装饰：可使用原创的手绘星点、纸胶带短线、叶片或像素小符号；每屏最多出现一种装饰语汇，且 `aria-hidden="true"`。

## 3. Color System

### 基础与语义 Token

| Token | Value | Role | Pairing / Constraint |
|---|---:|---|---|
| `--color-canvas` | `#FFF8EF` | 页面画布 | 搭配 `--color-text-primary` |
| `--color-surface` | `#FFFCF8` | 默认卡片、正文 | 不叠加透明白制造灰雾 |
| `--color-surface-soft` | `#F8EBDD` | Profile、筛选区、辅助块 | 搭配主/次文字 |
| `--color-surface-blush` | `#FBE2DC` | Moment 引用、温暖提示 | 不作为错误状态 |
| `--color-surface-dark` | `#2D2423` | 代码块、深色媒体控制 | 搭配 `#FFF7EE` |
| `--color-text-primary` | `#352724` | 标题、正文 | 主浅色面上的默认文本 |
| `--color-text-secondary` | `#6B514B` | 摘要、元信息 | 小字号不得低于 14px |
| `--color-text-inverse` | `#FFF7EE` | Hero、代码深色面文本 | 需要遮罩保证对比 |
| `--color-accent` | `#B9473D` | 主动作、当前筛选、链接 | 单屏实色面积建议小于 10%；在画布上对比约 4.94:1 |
| `--color-accent-hover` | `#96372F` | hover | 不通过缩放代替颜色反馈 |
| `--color-accent-pressed` | `#782B25` | pressed | 与 hover 状态可辨 |
| `--color-accent-soft` | `#F8D5CE` | 选中背景、标签底 | 文本使用 `#7B302A` |
| `--color-apricot` | `#EFA36D` | 次级装饰、Moment 标识 | 不用于长文本 |
| `--color-border` | `#E7D7C8` | 分隔、卡片描边 | 默认 1px |
| `--color-border-strong` | `#CDB8A7` | 输入框、强调分隔 | focus 时改用焦点环 |
| `--color-success` | `#477A5A` | 成功反馈 | 同时提供图标/文字 |
| `--color-warning` | `#9A641E` | 警告反馈 | 浅底使用 `#FFF1D8` |
| `--color-error` | `#B33F39` | 错误反馈 | 同时提供错误文本 |

### 交互与叠层

- `--color-focus-ring: #296FA8`：所有交互元素使用 `0 0 0 3px rgba(41, 111, 168, .28)`，不被暖色体系吞没。
- `--color-selection: #F2B9AD`：文本选择背景；选择文字保持 `#352724`。
- Hero 遮罩：底部 `rgba(35, 24, 22, .68)`，顶部 `rgba(35, 24, 22, .14)`，确保字幕和控制对比至少 4.5:1。
- disabled：文字 `#9B8880`、背景 `#EFE5DC`、描边 `#DDCFC3`；同时取消阴影并使用 `cursor: not-allowed`。
- 点赞后的珊瑚色必须同时伴随填充心形与可访问名称变化，不能只变色。

## 4. Typography

### 字体策略

- UI/正文：`"Noto Sans SC", "Microsoft YaHei", "PingFang SC", system-ui, sans-serif`。
- 技术标题/引语可选：`"Noto Serif SC", "Songti SC", SimSun, serif`；仅用于标题和短引语，不用于导航或小字号元信息。
- 代码：`"JetBrains Mono", "Cascadia Code", Consolas, monospace`。
- 第一阶段不依赖远程字体。若后续自托管字体，只允许 OFL 或项目已获授权字体，并使用 `font-display: swap`。

| Role | Desktop | Mobile | Weight | Line height | Tracking |
|---|---:|---:|---:|---:|---:|
| Hero display | `clamp(2.75rem, 6vw, 5.5rem)` | 同一 clamp | 700 | 1.02 | `-0.04em` |
| Page H1 | `2.75rem` | `2rem` | 700 | 1.15 | `-0.025em` |
| H2 | `2rem` | `1.625rem` | 700 | 1.25 | `-0.015em` |
| H3 / card title | `1.375rem` | `1.25rem` | 650–700 | 1.35 | `-0.01em` |
| Body | `1rem` | `1rem` | 400 | 1.75 | `0` |
| Article body | `1.0625rem` | `1rem` | 400 | 1.85 | `0` |
| Meta / UI | `0.875rem` | `0.875rem` | 500–600 | 1.45 | `0.015em` |
| Eyebrow (`TECH`, `MOMENT`) | `0.75rem` | `0.75rem` | 700 | 1.3 | `0.11em` |
| Code | `0.875rem` | `0.8125rem` | 400 | 1.7 | `0` |

- 技术正文宽度：`min(100%, 72ch)`；中文内容视觉目标约 32–38 字/行。
- 段落间距：`1em`；小标题上边距至少 `2em`，下边距 `0.65em`。
- 链接在正文中使用下划线，`text-underline-offset: .18em`；颜色不是唯一识别方式。
- 中文正文不使用全大写；英文类型标识可全大写。

## 5. Layout

- 全局容器：`min(100% - 2 * var(--page-gutter), 1200px)`。
- 页面 gutter：桌面 `32px`，平板 `24px`，手机 `16px`。
- 桌面网格：12 列，gap `24px`。首页 Feed 主栏 8 列，辅助栏 4 列；Profile 横跨 12 列。
- 技术正文页：正文居中，正文 `72ch`；目录在 `>= 1180px` 时固定于右侧 220px，不挤压正文。
- Hero 全宽但内部文本对齐 1200px 容器；视觉高度 `clamp(480px, 68vh, 720px)`。
- Profile 在桌面与 Hero 底部重叠 `56px`；手机重叠 `24px`，避免头像遮挡 Hero 文案。
- 首页 section 间距：桌面 `96px`，手机 `64px`；Feed 条目间距 `24px`。
- z-index 命名层：`base: 0`、`raised: 10`、`sticky: 30`、`overlay: 50`、`modal: 70`、`toast: 90`。禁止任意四位数。

### 页面清单与信息架构

- `/`：Navbar → Video Hero → Profile → Feed filters → Mixed Feed → Guestbook Preview → Footer。
- `/moments`：说说流；不强制标题，只提供筛选/月份分组和单条动态。
- `/tech`：技术文章列表、分类与标签。
- `/tech/:slug`：文章头图/摘要、元信息、Markdown 正文、目录、代码块、评论区壳子。
- `/guestbook`：空间留言说明、留言表单壳子、留言列表；不与内容评论混用。
- `/archive`：按年份/月组织 TECH 与 MOMENT，类型清晰可辨。
- `/about`：个人资料、技能与空间说明。

## 6. Navbar

- 桌面高度 `72px`，固定在 Hero 顶部；Hero 区域使用透明深色叠层，离开 Hero 后转为 `rgba(255, 252, 248, .92)` + `backdrop-filter: blur(14px)` + 底部描边。
- 品牌“玢的空间”位于左侧，导航：首页、说说、技术、留言板、归档、关于。品牌不可呈现为企业 Logo 锁定组合。
- 当前路由使用 2px 珊瑚下划线与 `aria-current="page"`；hover 只改变文字和短下划线，不让整项跳动。
- 桌面导航链接最小点击区域 `44 × 44px`。键盘顺序：跳到正文 → 品牌 → 导航 → Hero 控制。
- `< 768px` 使用“空间菜单”按钮打开右侧/顶部抽屉；按钮有文字或可访问名称，不只显示汉堡图标。
- 移动抽屉打开后锁定背景滚动、管理焦点、支持 Escape 关闭；不得把导航做成后台式侧栏。

## 7. Anime Video Hero

- 视频是背景层而非播放器卡片：全宽、`object-fit: cover`，内容容器覆盖其上，底部自然过渡到页面画布。
- 属性：`autoplay muted loop playsinline preload="metadata"`。永远默认静音，不尝试绕过浏览器策略。
- 右下角保留 `SOUND OFF / ON` 控制，最小 `44 × 44px`；切换声音必须由用户手势触发，图标旁保留可见文字。
- 必须提供 poster。自动播放失败、视频未加载或保存数据模式开启时直接显示 poster，不出现破损播放器。
- 文案不超过 2 行主标题 + 2 行描述；文本宽度不超过 620px。避免营销式“立即开始”主 CTA，可提供低权重“向下看看”锚点。
- 视频内容建议：自有/获授权的日常动漫氛围片段，如窗边、云、树影、桌面、城市黄昏；镜头位移缓慢，避免快速闪烁、战斗画面与角色大特写抢占身份表达。
- `prefers-reduced-motion: reduce` 时不自动播放，显示 poster；页面失焦或 Hero 离开视口时暂停，返回时仅在原本处于自动播放状态下恢复。

## 8. Profile

- 使用一张横向暖白卡片叠在 Hero 底部：半径 `28px`、描边 1px、`shadow-2`，桌面 padding `32px`，手机 `20px`。
- 头像桌面 `112px`、手机 `84px`，圆形但带 4px 画布色外环；必须有有意义的 alt，若只是重复姓名则使用空 alt。
- 信息顺序：`玢` → `Java Backend Developer` → “记录代码，也记录生活。”。职业是辅助信息，不能比姓名更强。
- 统计：技术文章、说说、留言三项，数值使用等宽数字；整个统计项若可点击则由一个链接包裹，避免嵌套按钮。
- 桌面为头像 / 自介 / 统计三段；手机为头像与姓名同行、简介换行、统计均分三列。
- 不加入等级、黄钻、访客排行等 QQ 专属视觉符号；借鉴的是“空间主人在场”的信息结构。

## 9. Feed

- Feed 顶部先出现标题“最近更新”和三项筛选：全部、说说、技术。使用 `button` 或有路由意义的链接，并声明选中状态；不是装饰标签。
- 筛选条可在移动端横向滚动，但首屏必须完整看见三项。按钮高度 `40px`，胶囊半径 `999px`。
- “全部”按发布时间倒序混排。切换筛选时保留滚动附近位置，不把用户弹回页面顶部。
- 内容列表使用语义化 `<section>` + `<article>`；Feed 自身不使用无限滚动，首期可用“查看更多”。
- 筛选过渡只做 `opacity 160ms` + `translateY(4px)`；加载时使用保留实际高度的骨架，空态说明原因并提供回到“全部”的动作。

## 10. TECH Post

### Feed Card

- 结构：`TECH` eyebrow → 标题 → 分类/标签/阅读时间/日期 → 摘要 → 可选封面 → “阅读全文” → 点赞/评论。
- 标题是卡片唯一主链接；“阅读全文”可指向同一路由但保持相同可访问名称语境。卡片整体不得包在单个链接中，以免点赞/评论嵌套交互。
- 桌面有封面时采用文字 7 / 图片 5 的横向布局；图片比例 `4:3`。手机改为图片在上、正文在下。
- 默认 surface 白、1px 描边、半径 `22px`、padding `28px`；hover 时边框加深并上移 `2px`，不改变尺寸。
- 摘要最多 3 行但完整内容仍存在于详情页；不要用过度模糊的 AI 文案占位。

### Article Detail

- 正文支持 Markdown 映射后的语义 HTML、2–4 级标题、目录、引用、列表、表格、图片和代码块。
- 目录高亮当前章节；键盘可用，锚点跳转考虑 sticky Navbar 的 `scroll-margin-top: 96px`。
- 代码块使用深棕面、顶部语言标识和复制按钮；行过长水平滚动，不缩小到不可读。复制成功用短文本状态并通过 `aria-live="polite"` 通知。
- 表格在窄屏放进可水平滚动容器，并提供可聚焦区域说明。
- 文章评论属于该文章，位于正文结束之后，标题明确为“这篇文章的评论”；不得复用留言板标题或文案。

## 11. MOMENT Post

- 不强制标题。卡片开头直接呈现正文，类型 `MOMENT` 和时间作为较弱元信息。
- 正文保持自然段落，Feed 中建议 280 个中文字符以内；更长内容使用“展开”，且展开后原地显示。
- 单图：`min(100%, 620px)`，比例可在 `4:3`、`3:4`、`1:1` 内自适应，最大高度 680px。
- 双图：等宽两列；三图：一张大图 + 两张小图；四张及以上使用 2×2 预览并在末图显示剩余数量。手机 gap `6px`，桌面 `8px`。
- 图片点击进入可访问的 lightbox；支持 Escape、焦点管理、上一张/下一张按钮及图片说明。
- 点赞与评论位于底部操作行，使用图标 + 数字；未登录 Mock 阶段只表现本地交互状态并明确不持久化，不伪装真实提交成功。
- Moment 使用 `--color-surface` 配合左上角短杏色标记，不能靠整张粉色卡片区分类型。

## 12. Guestbook

### 与 Comment 的边界

- `Comment` 从属于某一篇 TECH 或某一条 MOMENT，界面始终显示所评论内容的上下文。
- `Guestbook` 面向空间主人，不属于任何内容；路由固定为 `/guestbook`，标题与提示使用“给玢留言”。
- 数据模型、页面标题、空态、提交提示和导航入口均不得共用“评论/留言”混合文案。

### Guestbook Preview

- 首页预览使用与 Feed 不同的 `--color-surface-soft` 宽区块，展示 3 条最新留言、头像/昵称/时间/短文本，以及“去留言板”。
- 留言像纸条但不随机旋转，避免可读性与布局抖动；可通过浅色分隔和 18px 圆角表达亲密感。

### `/guestbook`

- 顶部先解释留言会留给空间主人；表单字段首期可为昵称、留言内容，均有永久可见 `<label>`。
- 文本域最小高度 144px，字符计数不替代错误提示；提交按钮有 loading、success、error、disabled 状态。
- Mock 提交明确标为演示状态；刷新后丢失时提前告知。
- 留言列表不显示与具体文章关联的“回复原文”；未来主人回复使用缩进的“玢 回复”结构。

## 13. Cards

| Variant | Radius | Padding | Border / Shadow | Interaction |
|---|---:|---:|---|---|
| Profile | `28px` | `32px / 20px` | border + `shadow-2` | 内部链接独立聚焦 |
| TECH | `22px` | `28px / 20px` | border + `shadow-1` | hover/focus-within 上移 2px |
| MOMENT | `22px` | `24px / 18px` | border + `shadow-1` | 操作行独立，不整卡可点 |
| Guestbook note | `18px` | `18px` | border，无默认阴影 | hover 只加深边框 |
| Code block | `16px` | `20px` | 深色面，无阴影 | 复制按钮有完整状态 |

- loading：骨架使用 `--color-surface-soft`，不闪烁；可做 1.6s 低对比渐变，减少动态时禁用。
- empty：保留区块标题，给出一句具体原因和一个恢复动作。
- error：在组件原位显示错误文本与重试按钮；不可仅用 toast。
- 卡片阴影：`shadow-1: 0 8px 24px rgba(80, 53, 42, .06)`；`shadow-2: 0 18px 48px rgba(80, 53, 42, .12)`。

## 14. Media

- Hero 视频优先 WebM + MP4 fallback，poster 使用 AVIF/WebP + JPEG fallback；视频首期建议控制在 8–12 MB 以内并提供移动端较低码率源。
- Feed 图片使用 `<picture>`、明确 `width`/`height` 或 `aspect-ratio` 防止布局偏移；首屏外 `loading="lazy"`、`decoding="async"`。
- 头像 `1:1`，TECH 封面 `4:3`，Guestbook 头像 `1:1`；内容图片不强裁切人物脸部，支持通过 Mock Data 提供 `object-position`。
- 图片 alt 描述内容与语境，不写“图片”；纯装饰为空 alt。
- 视频若承载信息必须提供字幕或同等文本；纯氛围 Hero 可静音循环，但需要可见的暂停/播放和声音控制。
- 浏览器 `Save-Data` 或慢速网络时默认 poster；不得为了 Hero 阻塞 Navbar/Profile 与正文渲染。
- 任何动漫素材必须是项目自制、明确授权或许可兼容素材，保留来源与许可证记录。

## 15. Motion

| Motion | Trigger | Property | Duration | Easing | Reduced-motion fallback |
|---|---|---|---:|---|---|
| Navbar surface | 离开 Hero | background, border-color | `180ms` | `ease-out` | 直接切换 |
| Feed filter | 筛选变化 | opacity, translateY `4px` | `160ms` | `cubic-bezier(.2,.8,.2,1)` | 仅 opacity 或直接切换 |
| Card lift | hover / focus-within | transform `-2px`, shadow | `180ms` | `ease-out` | 只改变边框 |
| Like feedback | 用户点击 | heart scale `1 → 1.12 → 1` | `220ms` | `cubic-bezier(.2,.8,.2,1)` | 无缩放，保留填充/文字变化 |
| Lightbox | 打开/关闭 | opacity, image scale `.98 → 1` | `200ms` | `ease-out` | 仅 opacity 或直接显示 |
| Section reveal | 首次入视口 | opacity, translateY `12px` | `320ms` | `cubic-bezier(.2,.8,.2,1)` | 初始即显示 |

- 装饰动画不得无限漂浮；Hero 视频是唯一允许持续动态的大面积元素。
- 不动画 `height`、`width`、`top`、`left` 等容易造成布局抖动的属性。
- 动画不延迟主要内容可用时间，单次交互反馈不超过 320ms。

## 16. Spacing

- 基础单位：`4px`。
- 间距 scale：`--space-1: 4px`、`2: 8px`、`3: 12px`、`4: 16px`、`5: 20px`、`6: 24px`、`8: 32px`、`10: 40px`、`12: 48px`、`16: 64px`、`20: 80px`、`24: 96px`。
- 半径 scale：`--radius-sm: 10px`、`md: 16px`、`lg: 22px`、`xl: 28px`、`pill: 999px`。
- 描边：默认 `1px solid var(--color-border)`；选中/错误可用 2px，但必须预留空间避免跳动。
- 图标与文字 gap 使用 6–8px；卡片内部大分组 20–28px；不得出现无 token 的 13px、19px 等随机间距。

## 17. Responsive

| Range | Grid / Gutters | Navigation | Content Order | Media Behavior |
|---|---|---|---|---|
| `>= 1180px` | 12 列，32px gutter，24px gap | 完整导航，sticky | Feed 8 列 + 辅助 4 列；文章目录右置 | Hero 最高 720px，TECH 横卡 |
| `768–1179px` | 8 列，24px gutter，20px gap | 768px 起可保留精简导航，否则抽屉 | Feed 单栏；Guestbook Preview 在其后 | Hero 56–64vh；TECH 可横向但图片 40% |
| `480–767px` | 4 列，16px gutter，16px gap | 菜单按钮 + 抽屉 | 全部单栏；Profile 重排 | Hero 最低 480px；TECH 图片置顶 |
| `320–479px` | 4 列，16px gutter，12px gap | 同上，品牌名可缩为“玢的空间” | 统计三等分，操作行允许换行 | Hero 使用 poster 优先；Moment 双列图片仍保留 |

- 断点由布局承载能力决定，不按具体设备命名。
- 200% 缩放和 320 CSS px 宽度下不出现页面级横向滚动；代码块/表格允许组件内部滚动。
- 移动端内容顺序始终为：Hero → Profile → Feed → Guestbook，不能因桌面侧栏而改变语义阅读顺序。
- Profile 重叠不得造成滚动时内容被 sticky Navbar 遮挡。

## 18. Accessibility

- 目标：WCAG 2.2 AA。普通文字对比至少 4.5:1，大号粗体至少 3:1，交互边界/焦点至少 3:1。
- 页面首个可聚焦元素为“跳到主要内容”；使用 `header/nav/main/section/article/footer` 地标和连续的标题层级。
- 所有交互元素提供 `:focus-visible`，焦点环不被 `overflow: hidden` 裁切；不移除 outline 而无替代。
- 触摸目标至少 `44 × 44px`，相邻小目标间距至少 8px。
- 筛选使用可识别的按钮组/标签语义；动态结果数量变化通过 `aria-live="polite"` 简短通知。
- 点赞按钮的可访问名称包含当前状态；评论数链接说明目标内容。
- 抽屉和 lightbox 管理初始焦点、焦点圈定、Escape 关闭和关闭后的焦点恢复。
- Hero 提供暂停；`prefers-reduced-motion` 停止自动视频与装饰 reveal；保存数据偏好使用 poster。
- 表单错误与字段通过 `aria-describedby` 关联；placeholder 不替代 label。
- 支持浏览器字体放大与 200% 页面缩放；内容不因固定高度被裁切。

## 19. Do

- 让视频建立情绪，让正文保持安静；Hero 与 Feed 使用不同的信息密度。
- 用结构、标题层级和媒体布局共同区分 TECH / MOMENT。
- 使用真实、有生活语气的 Mock Data 验证长短内容、图片数量和中文换行。
- 把 Comment 和 Guestbook 当作两个不同产品概念编写组件名、文案和路由。
- 为每个媒体资源准备 poster/占位、尺寸、alt 和许可信息。
- 优先使用 CSS 自定义属性与小型 Vue 组件，不引入大型 UI 框架。
- 先完成语义 DOM、响应式和键盘流程，再增加装饰与动效。

## 20. Don't

- 不做廉价粉色二次元模板，不使用未经许可的动漫角色、Logo、截图或音乐。
- 不做游戏官网式霓虹 HUD、巨型发光按钮、粒子雨或高频闪烁。
- 不做 SaaS Hero、Dashboard 指标面板、后台侧栏或企业功能网格。
- 不用大面积珊瑚色淹没页面，不用纯黑正文，不用低对比浅灰承载信息。
- 不把所有内容塞进一模一样的卡片；TECH 与 MOMENT 必须有各自结构。
- 不给 MOMENT 强行补标题，不把留言板称为评论区。
- 不自动播放有声视频，不隐藏暂停控制，不绕过浏览器自动播放限制。
- 不复制参考品牌的色板、字体、标志性布局、图标或插画。
- 不在本阶段创建 Spring Boot、API、数据库、认证或任何后端目录。

## Existing UI to Keep

- 保留 Vue 3 + Vite + JavaScript + Vue Router + Composition API 技术栈。
- 保留 `/` 首页路由和 `RouterView` 根结构；当前占位页只用于确认初始化成功。
- 保留 `src/assets/main.css` 中的 box-sizing、最小页面尺寸和表单继承字体等基础 reset，可在实现时迁移到 token 层。

## Existing UI to Refactor

- `HomeView.vue` 当前仅是初始化占位内容；设计确认后按首页信息架构拆分，而非在一个文件中堆叠所有区块。
- `main.css` 当前颜色仅为初始化近似值；实现阶段应替换为本文完整语义 token，保留 reset 行为。
- Router 当前只有 `/`；实现阶段增加 `/moments`、`/tech`、`/tech/:slug`、`/guestbook`、`/archive`、`/about`，并保留 history 模式。

## 21. Implementation Checklist（设计确认后执行）

1. 在 `main.css` 建立本文 color、spacing、radius、shadow、container token 与 reset。
2. 建立 `AppNavbar`、`AppFooter`、`VideoHero`、`ProfilePanel`、`FeedFilter` 基础组件。
3. 建立独立的 `TechPostCard` 与 `MomentPostCard`，用同一份 Mock Data 联合类型渲染。
4. 定义 `mock/` 数据结构，明确 Comment 与 Guestbook 的不同关联字段。
5. 完成首页移动优先布局，再扩展桌面 12 列结构与 Profile 重叠。
6. 增加 Hero poster、播放/暂停、声音控制、自动播放失败和 reduced-motion / Save-Data 分支。
7. 增加 `/tech`、`/tech/:slug`、`/moments`、`/guestbook`、`/archive`、`/about` 页面壳子。
8. 实现所有 hover、focus-visible、loading、empty、error、disabled 状态。
9. 用 320px、768px、1180px、1440px 和 200% 缩放做视觉/键盘验证。
10. 检查颜色对比、Heading 顺序、landmark、触摸尺寸、媒体 alt/字幕和素材许可证。
