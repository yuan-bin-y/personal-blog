<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import TaxonomyManager from '../components/owner/TaxonomyManager.vue'
import MediaLibrary from '../components/owner/MediaLibrary.vue'
import MediaPicker from '../components/media/MediaPicker.vue'
import { apiMessage } from '../api/request'
import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const { isOwner, initializeOwnerMode, logoutOwner } = useOwnerMode()
const { state, initialize, loadPublicFeed, updateProfile, updateHero, updateMusic, updateAnnouncement, updateBasic, updateAppearance, updatePageMedia } = useSpaceRuntime()
const saved = reactive({ profile: false, hero: false, music: false, announcement: false, basic: false, appearance: false, media: false })
const error = ref('')

const profileDraft = reactive({
  name: state.profile.name,
  avatar: state.profile.avatar,
  role: state.profile.role,
  bio: state.profile.bio,
  statusText: state.profile.status?.text || '',
})
const heroDraft = reactive({
  desktopVideo: state.hero.media?.desktop?.mp4 || '',
  mobileVideo: state.hero.media?.mobile?.mp4 || '',
  poster: state.hero.media?.poster?.fallback || '',
  heroTitle: state.hero.title,
  heroSubtitle: state.hero.description,
})
const musicDraft = reactive({ title: state.music.title, artist: state.music.artist, audioUrl: state.music.audioUrl || state.music.src, cover: state.music.cover })
const announcementDraft = reactive({ content: '', enabled: false })
const basicDraft = reactive({ ...state.basic })
const appearanceDraft = reactive({ ...state.appearance, wallpaper: { ...state.appearance.wallpaper } })
const pageMediaDraft = reactive(JSON.parse(JSON.stringify(state.pageMedia)))

const syncDrafts = () => {
  Object.assign(profileDraft, { name: state.profile.name, avatar: state.profile.avatar || '', role: state.profile.role || '', bio: state.profile.bio || '', statusText: state.profile.status?.text || '' })
  Object.assign(heroDraft, { desktopVideo: state.hero.media?.desktop?.mp4 || '', mobileVideo: state.hero.media?.mobile?.mp4 || '', poster: state.hero.media?.poster?.fallback || '', heroTitle: state.hero.title || '', heroSubtitle: state.hero.description || '' })
  Object.assign(musicDraft, { title: state.music.title || '', artist: state.music.artist || '', audioUrl: state.music.audioUrl || state.music.src || '', cover: state.music.cover || '' })
  Object.assign(announcementDraft, { content: state.announcement?.content || '', enabled: Boolean(state.announcement?.enabled) })
  Object.assign(basicDraft, state.basic)
  Object.assign(appearanceDraft, { ...state.appearance, wallpaper: { ...state.appearance.wallpaper } })
  Object.keys(pageMediaDraft).forEach((key) => delete pageMediaDraft[key])
  Object.assign(pageMediaDraft, JSON.parse(JSON.stringify(state.pageMedia)))
}

const flash = (key) => {
  saved[key] = true
  window.setTimeout(() => { saved[key] = false }, 2200)
}
const save = async (key, action) => {
  error.value = ''
  try { await action(); flash(key) } catch (reason) { error.value = apiMessage(reason) }
}
const saveProfile = () => save('profile', () => updateProfile({ name: profileDraft.name, avatar: profileDraft.avatar || null, role: profileDraft.role, bio: profileDraft.bio, status: { label: state.profile.status?.label || '空间状态', text: profileDraft.statusText, emoji: state.profile.status?.emoji || '…' } }))
const saveHero = () => save('hero', () => updateHero({ eyebrow: state.hero.eyebrow || '', heroTitle: heroDraft.heroTitle, heroSubtitle: heroDraft.heroSubtitle, desktopVideo: heroDraft.desktopVideo || null, mobileVideo: heroDraft.mobileVideo || null, poster: heroDraft.poster || null }))
const saveMusic = () => save('music', () => updateMusic({ ...musicDraft }))
const saveAnnouncement = () => save('announcement', () => updateAnnouncement({ ...announcementDraft }))
const saveBasic = () => save('basic', () => updateBasic({ name: basicDraft.name, chineseName: basicDraft.chineseName, description: basicDraft.description }))
const saveAppearance = () => save('appearance', () => updateAppearance({ ...appearanceDraft, surfaceOpacity: Number(appearanceDraft.surfaceOpacity), backdropShade: Number(appearanceDraft.backdropShade), wallpaper: { ...appearanceDraft.wallpaper } }))
const savePageMedia = () => save('media', () => updatePageMedia(JSON.parse(JSON.stringify(pageMediaDraft))))
const leaveOwnerMode = async () => { await logoutOwner(); await loadPublicFeed() }

onMounted(async () => {
  await Promise.all([initializeOwnerMode(), initialize()])
  syncDrafts()
})
</script>

<template>
  <main id="main-content" class="settings-page">
    <template v-if="isOwner">
      <header class="settings-intro"><p>OWNER MODE</p><h1>把空间整理成喜欢的样子</h1><span>保存后会写入 BinSpace 数据库。这里不是独立后台，而是主人视角下的空间设置。</span></header>
      <p v-if="error" class="settings-error">{{ error }}</p>
      <nav class="settings-index" aria-label="设置目录"><a href="#profile">资料</a><a href="#hero">背景</a><a href="#announcement">公告</a><a href="#music">音乐</a><a href="#basic">基本信息</a><a href="#appearance">外观</a><a href="#page-media">页面媒体</a><a href="#media-library">媒体库</a><a href="#taxonomy">分类标签</a></nav>

      <section id="profile" class="settings-section"><header><span>01</span><div><h2>个人资料</h2><p>像更换空间头像一样直接选择图片，上传完成后再保存资料。</p></div></header><form @submit.prevent="saveProfile"><MediaPicker v-model="profileDraft.avatar" class="settings-section__wide" usage-type="AVATAR" media-type="IMAGE" label="空间头像" hint="建议使用正方形图片，上传后会自动预览。" /><label>名字<input v-model="profileDraft.name" required /></label><label>角色<input v-model="profileDraft.role" /></label><label class="settings-section__wide">简介<textarea v-model="profileDraft.bio" rows="3"></textarea></label><label class="settings-section__wide">当前状态<input v-model="profileDraft.statusText" /></label><footer><span v-if="saved.profile">头像与资料已保存</span><button type="submit">保存资料</button></footer></form></section>

      <section id="hero" class="settings-section"><header><span>02</span><div><h2>Hero 背景</h2><p>桌面与手机可以使用不同视频；海报会在视频加载前以及节省流量模式下显示。</p></div></header><form @submit.prevent="saveHero"><MediaPicker v-model="heroDraft.desktopVideo" class="settings-section__wide" usage-type="HERO" media-type="VIDEO" label="桌面端 Hero 视频" hint="建议 16:9，后台会负责文件安全校验与 OSS 保存。" /><MediaPicker v-model="heroDraft.mobileVideo" usage-type="HERO" media-type="VIDEO" label="移动端 Hero 视频" hint="可选；留空时使用桌面视频。" /><MediaPicker v-model="heroDraft.poster" usage-type="HERO" media-type="IMAGE" label="Hero 海报" hint="视频尚未播放时显示。" /><label class="settings-section__wide">Hero Title<input v-model="heroDraft.heroTitle" /></label><label class="settings-section__wide">Hero Subtitle<textarea v-model="heroDraft.heroSubtitle" rows="3"></textarea></label><footer><span v-if="saved.hero">背景配置已保存</span><button type="submit">保存背景配置</button></footer></form></section>

      <section id="announcement" class="settings-section"><header><span>03</span><div><h2>空间公告</h2><p>可以单独关闭公告，而不删除已有文字。</p></div></header><form @submit.prevent="saveAnnouncement"><label class="settings-section__wide">公告内容<textarea v-model="announcementDraft.content" rows="4"></textarea></label><label class="settings-check"><input v-model="announcementDraft.enabled" type="checkbox" /> 启用公告</label><footer><span v-if="saved.announcement">已保存到数据库</span><button type="submit">保存公告</button></footer></form></section>

      <section id="music" class="settings-section"><header><span>04</span><div><h2>空间音乐</h2><p>直接选择音频和封面，保存后成为当前空间音乐。</p></div></header><form @submit.prevent="saveMusic"><label>标题<input v-model="musicDraft.title" /></label><label>作者<input v-model="musicDraft.artist" /></label><MediaPicker v-model="musicDraft.audioUrl" class="settings-section__wide" usage-type="MUSIC" media-type="AUDIO" label="音乐文件" hint="支持格式和大小以后端媒体校验为准。" /><MediaPicker v-model="musicDraft.cover" class="settings-section__wide" usage-type="MUSIC" media-type="IMAGE" label="音乐封面" /><footer><span v-if="saved.music">音乐配置已保存</span><button type="submit">保存音乐</button></footer></form></section>

      <section id="basic" class="settings-section"><header><span>05</span><div><h2>空间基本信息</h2><p>站点名称与一句话说明。</p></div></header><form @submit.prevent="saveBasic"><label>英文名<input v-model="basicDraft.name" /></label><label>中文名<input v-model="basicDraft.chineseName" /></label><label class="settings-section__wide">说明<input v-model="basicDraft.description" /></label><footer><span v-if="saved.basic">已保存到数据库</span><button type="submit">保存基本信息</button></footer></form></section>

      <section id="appearance" class="settings-section"><header><span>06</span><div><h2>默认空间外观</h2><p>Visitor 首次进入首页时采用的布局与背景。</p></div></header><form @submit.prevent="saveAppearance"><label>布局模式<select v-model="appearanceDraft.layoutMode"><option value="standard">标准首页</option><option value="immersive">沉浸首页</option></select></label><label>背景模式<select v-model="appearanceDraft.backgroundMode"><option value="wallpaper">壁纸</option><option value="video">视频</option></select></label><label>内容不透明度<input v-model.number="appearanceDraft.surfaceOpacity" type="number" min="0" max="1" step="0.02" /></label><label>背景遮罩<input v-model.number="appearanceDraft.backdropShade" type="number" min="0" max="1" step="0.02" /></label><MediaPicker v-model="appearanceDraft.wallpaper.desktop" usage-type="PAGE_BACKGROUND" media-type="IMAGE" label="桌面首页壁纸" /><MediaPicker v-model="appearanceDraft.wallpaper.mobile" usage-type="PAGE_BACKGROUND" media-type="IMAGE" label="移动首页壁纸" /><label class="settings-check"><input v-model="appearanceDraft.allowVisitorControls" type="checkbox" /> 允许 Visitor 调整当前外观</label><footer><span v-if="saved.appearance">默认外观已保存</span><button type="submit">保存默认外观</button></footer></form></section>

      <section id="page-media" class="settings-section"><header><span>07</span><div><h2>页面媒体</h2><p>给说说、留言板、技术、归档和关于分别选择背景；可选视频会覆盖静态海报。</p></div></header><form @submit.prevent="savePageMedia"><fieldset v-for="(item, key) in pageMediaDraft" :key="key" class="settings-media"><legend>{{ key }}</legend><MediaPicker v-model="item.video" usage-type="PAGE_BACKGROUND" media-type="VIDEO" label="动态背景" /><MediaPicker v-model="item.poster" usage-type="PAGE_BACKGROUND" media-type="IMAGE" label="静态背景 / 海报" /><MediaPicker v-model="item.overlay" usage-type="PAGE_BACKGROUND" media-type="IMAGE" label="可选叠加层" /><label>动态方式<input v-model="item.motion" placeholder="例如 drift / breathe" /></label><label>环境效果<input v-model="item.effect" placeholder="例如 petals / ripple" /></label></fieldset><footer><span v-if="saved.media">页面媒体已保存</span><button type="submit">保存页面媒体</button></footer></form></section>

      <section id="media-library" class="settings-section settings-section--stack"><header><span>08</span><div><h2>空间媒体库</h2><p>用于查找、复用和删除历史素材；平时直接在对应设置中选择文件即可。</p></div></header><MediaLibrary /></section>

      <section id="taxonomy" class="settings-section settings-section--stack"><header><span>09</span><div><h2>分类与标签</h2><p>停用后不再作为新文章候选项，历史文章仍保留名称。</p></div></header><TaxonomyManager /></section>

      <aside class="settings-preview"><div><strong>退出主人模式</strong><span>退出后将清除当前浏览器会话中的 Access Token，并恢复公开内容列表。</span></div><button type="button" @click="leaveOwnerMode">退出登录</button></aside>
    </template>
      <section v-else class="settings-visitor"><p>VISITOR MODE</p><h1>这是空间主人的设置页</h1><span>当前浏览身份没有管理权限。</span><div><RouterLink to="/owner-login">主人登录</RouterLink><RouterLink to="/">返回空间首页</RouterLink></div></section>
  </main>
</template>

<style scoped>
.settings-page { padding: var(--space-8); color: var(--color-text-primary); background: rgba(252,251,249,.94); border: 1px solid var(--color-border); border-radius: var(--radius-xl); box-shadow: var(--shadow-1); }
.settings-intro { max-width: 760px; padding-bottom: var(--space-8); }
.settings-intro p, .settings-visitor > p { margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: .12em; }
.settings-intro h1, .settings-visitor h1 { margin: 0; font-family: var(--font-serif); font-size: clamp(2rem, 4vw, 3.3rem); line-height: 1.15; }
.settings-intro > span, .settings-visitor > span { display: block; max-width: 660px; margin-top: var(--space-4); color: var(--color-text-secondary); line-height: 1.8; }
.settings-index { position: sticky; top: calc(var(--navbar-height) + var(--space-6)); z-index: 2; display: flex; gap: var(--space-2); padding: var(--space-3); overflow-x: auto; background: rgba(252,251,249,.92); border-block: 1px solid var(--color-border); backdrop-filter: blur(12px); }
.settings-index a { min-height: 38px; padding: 9px var(--space-4); border-radius: var(--radius-pill); color: var(--color-text-secondary); font-size: .8125rem; font-weight: 700; text-decoration: none; white-space: nowrap; }
.settings-index a:hover { color: var(--color-accent); background: var(--color-surface-soft); }
.settings-section { display: grid; grid-template-columns: 230px minmax(0,1fr); gap: var(--space-10); padding: var(--space-12) 0; border-bottom: 1px solid var(--color-border); scroll-margin-top: 150px; }
.settings-section > header { display: flex; align-items: flex-start; gap: var(--space-3); }
.settings-section > header > span { color: var(--color-accent); font-family: var(--font-mono); font-size: .6875rem; }
.settings-section h2 { margin: 0; font-family: var(--font-serif); font-size: 1.45rem; }
.settings-section header p { margin: var(--space-2) 0 0; color: var(--color-text-secondary); font-size: .8125rem; line-height: 1.6; }
.settings-section form { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
.settings-section label { display: grid; gap: var(--space-2); color: var(--color-text-secondary); font-size: .8125rem; font-weight: 700; }
.settings-section__wide { grid-column: 1 / -1; }
.settings-section input, .settings-section textarea { width: 100%; padding: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; font-weight: 400; resize: vertical; }
.settings-section select { width: 100%; min-height: 44px; padding: 0 var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; }
.settings-check { display: flex !important; align-items: center; grid-auto-flow: column; justify-content: start; }
.settings-check input { width: auto; }
.settings-media { grid-column: 1 / -1; display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-3); margin: 0; padding: var(--space-4); border: 1px dashed var(--color-border-strong); border-radius: var(--radius-md); }
.settings-media legend { padding: 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: .75rem; font-weight: 700; text-transform: uppercase; }
.settings-section--stack { grid-template-columns: 1fr; }
.settings-error { position: sticky; top: calc(var(--navbar-height) + var(--space-2)); z-index: 3; padding: var(--space-3) var(--space-4); color: var(--color-error); background: var(--color-surface); border-left: 3px solid currentColor; }
.settings-section footer { grid-column: 1 / -1; display: flex; align-items: center; justify-content: flex-end; gap: var(--space-4); }
.settings-section footer span { color: var(--color-success); font-size: .8125rem; }
.settings-section button, .settings-preview button { min-height: 44px; padding: 0 var(--space-5); color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.settings-preview { display: flex; align-items: center; justify-content: space-between; gap: var(--space-6); margin-top: var(--space-10); padding: var(--space-5); background: var(--color-surface-soft); border-left: 3px solid var(--color-accent); }
.settings-preview div { display: grid; gap: var(--space-1); }.settings-preview span { color: var(--color-text-secondary); font-size: .8125rem; }
.settings-visitor { min-height: 52vh; display: grid; align-content: center; justify-items: start; max-width: 680px; }
.settings-visitor a { display: inline-flex; min-height: 44px; align-items: center; margin-top: var(--space-6); color: var(--color-accent); font-weight: 700; text-decoration: none; }
.settings-visitor > div { display: flex; gap: var(--space-4); flex-wrap: wrap; }
@media (max-width: 760px) { .settings-page { padding: var(--space-5); border-radius: var(--radius-lg); } .settings-section { grid-template-columns: 1fr; gap: var(--space-5); padding: var(--space-10) 0; } .settings-section form, .settings-media { grid-template-columns: 1fr; } .settings-section__wide, .settings-section footer, .settings-media { grid-column: auto; } .settings-preview { align-items: stretch; flex-direction: column; } }
</style>
