<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import MarkdownEditor from '@/components/editor/MarkdownEditor.vue'
import { postRepo } from '@/services'
import type { PublishStatus } from '@/types'
import { usePageMeta } from '@/composables/usePageMeta'
import { slugify } from '@/utils/slug'

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.name === 'author-post-new')
const postId = computed(() => (isNew.value ? '' : String(route.params.id)))

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const status = ref<PublishStatus>('draft')

const form = reactive({
  title: '',
  slug: '',
  summary: '',
  contentMd: '',
  coverUrl: '',
  tagsText: '',
})

usePageMeta({
  title: computed(() => (isNew.value ? '写文章' : form.title || '编辑文章')),
})

/** 用户手动改过 slug 后，不再随标题自动覆盖 */
let slugTouched = false
/** 当前已加载的路由键，避免新建保存后 replace 再打一次详情 */
let loadedKey = ''

function routeKey() {
  return isNew.value ? 'new' : postId.value
}

function resetForm() {
  form.title = ''
  form.slug = ''
  form.summary = ''
  form.contentMd = ''
  form.coverUrl = ''
  form.tagsText = ''
  status.value = 'draft'
  slugTouched = false
  error.value = ''
  loadedKey = ''
}

const ERROR_HINTS: Record<number, string> = {
  50302: '媒体存储不可用，请检查服务器 BLOG_STORAGE_ROOT 目录权限',
  40401: '正文文件缺失，请重新保存文章',
  40003: 'Slug 格式无效（仅小写字母、数字、连字符）',
  40902: 'Slug 已被占用，请换一个',
}

function formatApiError(e: unknown, fallback: string): string {
  if (e && typeof e === 'object' && 'code' in e && 'message' in e) {
    const code = Number((e as { code: number }).code)
    const message = String((e as { message: string }).message || '')
    return ERROR_HINTS[code] || message || fallback
  }
  return e instanceof Error ? e.message : fallback
}

function onTitleInput() {
  if (!slugTouched) {
    form.slug = slugify(form.title)
  }
}

function onSlugInput() {
  slugTouched = true
}

function tagsFromText() {
  return form.tagsText
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
}

function payload() {
  return {
    title: form.title,
    slug: form.slug,
    summary: form.summary,
    contentMd: form.contentMd,
    coverUrl: form.coverUrl || undefined,
    tags: tagsFromText(),
  }
}

async function load() {
  const key = routeKey()
  if (key === loadedKey) return

  if (isNew.value) {
    resetForm()
    loadedKey = 'new'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''
  try {
    const post = await postRepo.getMineById(postId.value)
    form.title = post.title
    form.slug = post.slug
    form.summary = post.summary
    form.contentMd = post.contentMd
    form.coverUrl = post.coverUrl || ''
    form.tagsText = post.tags.join(', ')
    status.value = post.status
    slugTouched = true
    loadedKey = key
  } catch (e) {
    error.value = formatApiError(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function saveDraft() {
  if (saving.value) return
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    if (isNew.value) {
      const created = await postRepo.create(payload())
      status.value = created.status
      slugTouched = true
      loadedKey = created.id
      await router.replace(`/author/posts/${created.id}/edit`)
    } else {
      const updated = await postRepo.update(postId.value, payload())
      status.value = updated.status
    }
    success.value = '草稿已保存'
  } catch (e) {
    error.value = formatApiError(e, '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish() {
  if (saving.value) return
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    if (isNew.value) {
      const created = await postRepo.create(payload())
      await postRepo.publish(created.id)
      status.value = 'published'
      slugTouched = true
      loadedKey = created.id
      await router.replace(`/author/posts/${created.id}/edit`)
    } else {
      await postRepo.update(postId.value, payload())
      const published = await postRepo.publish(postId.value)
      status.value = published.status
    }
    success.value = '已发布'
  } catch (e) {
    error.value = formatApiError(e, '发布失败')
  } finally {
    saving.value = false
  }
}

async function unpublish() {
  if (isNew.value || saving.value) return
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const result = await postRepo.unpublish(postId.value)
    status.value = result.status
    success.value = '已取消发布'
  } catch (e) {
    error.value = formatApiError(e, '取消发布失败')
  } finally {
    saving.value = false
  }
}

// 新建/编辑共用组件实例，必须随路由参数重载，避免串稿
watch(routeKey, () => {
  void load()
}, { immediate: true })
</script>

<template>
  <section class="section">
    <PageContainer>
      <div class="editor-head">
        <div>
          <h1 class="editor-title">{{ isNew ? '写文章' : '编辑文章' }}</h1>
          <p class="text-muted">
            {{ status === 'published' ? '已发布 · 可继续修改' : '草稿 · 边写边预览' }}
          </p>
        </div>
      </div>

      <p v-if="error" class="editor-error">{{ error }}</p>
      <p v-else-if="success" class="editor-success">{{ success }}</p>
      <LoadingState v-if="loading" />

      <form v-else class="editor-form" @submit.prevent>
        <label class="field">
          <span>标题</span>
          <input v-model="form.title" type="text" required maxlength="200" @input="onTitleInput" />
        </label>
        <label class="field">
          <span>Slug（小写字母、数字、连字符）</span>
          <input
            v-model="form.slug"
            type="text"
            required
            maxlength="200"
            pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
            title="仅支持小写字母、数字与连字符，如 my-first-post"
            @input="onSlugInput"
          />
        </label>
        <label class="field">
          <span>摘要</span>
          <textarea v-model="form.summary" rows="2" maxlength="500" />
        </label>
        <label class="field">
          <span>标签（逗号分隔）</span>
          <input v-model="form.tagsText" type="text" placeholder="vue, typescript" />
        </label>
        <label class="field">
          <span>封面 URL（可选）</span>
          <input v-model="form.coverUrl" type="url" placeholder="https://..." />
        </label>
        <div class="field">
          <span>正文</span>
          <MarkdownEditor v-model="form.contentMd" />
        </div>
        <div class="editor-actions editor-actions--footer">
          <BaseButton variant="secondary" type="button" :disabled="saving" @click="saveDraft">
            保存草稿
          </BaseButton>
          <BaseButton
            v-if="status === 'published'"
            variant="secondary"
            type="button"
            :disabled="saving"
            @click="unpublish"
          >
            取消发布
          </BaseButton>
          <BaseButton type="button" :disabled="saving" @click="publish">发布</BaseButton>
        </div>
      </form>
    </PageContainer>
  </section>
</template>

<style scoped>
.editor-head {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.editor-title {
  font-family: var(--font-display);
  font-size: clamp(1.75rem, 4vw, 2.25rem);
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.editor-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.editor-actions--footer {
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-line, var(--color-border));
}

.editor-error {
  color: var(--color-cinnabar, #b83b2d);
  margin-bottom: var(--spacing-md);
}

.editor-success {
  color: var(--color-ink-soft, #3d3d3d);
  margin-bottom: var(--spacing-md);
}

.editor-form {
  display: grid;
  gap: var(--spacing-lg);
}

.field {
  display: grid;
  gap: 0.5rem;
}

.field > span {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-ink-soft, var(--color-text));
}

.field input,
.field textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-line, var(--color-border));
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-ink, var(--color-text));
}

.field input:focus,
.field textarea:focus {
  outline: 2px solid color-mix(in srgb, var(--color-cinnabar, #b83b2d) 35%, transparent);
  outline-offset: 1px;
}

@media (max-width: 720px) {
  .editor-actions {
    width: 100%;
  }
}
</style>
