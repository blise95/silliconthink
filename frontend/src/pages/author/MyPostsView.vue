<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { postRepo } from '@/services'
import type { Post } from '@/types'
import { formatDate } from '@/utils/markdown'
import { usePageMeta } from '@/composables/usePageMeta'

usePageMeta({ title: '我的文章' })

const loading = ref(true)
const acting = ref(false)
const posts = ref<Post[]>([])
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await postRepo.listMine({ page: 1, pageSize: 50 })
    posts.value = page.list
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

/** 发布 / 取消发布 / 删除 共用：捕获错误并刷新列表 */
async function runAction(action: () => Promise<unknown>, fallback: string) {
  if (acting.value) return
  acting.value = true
  error.value = ''
  try {
    await action()
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : fallback
  } finally {
    acting.value = false
  }
}

function onPublish(id: string) {
  return runAction(() => postRepo.publish(id), '发布失败')
}

function onUnpublish(id: string) {
  return runAction(() => postRepo.unpublish(id), '取消发布失败')
}

function onRemove(id: string) {
  if (!confirm('确定删除这篇文章？')) return
  return runAction(() => postRepo.remove(id), '删除失败')
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <div class="author-head">
        <div>
          <h1 class="author-title">我的文章</h1>
          <p class="text-muted">草稿与已发布内容</p>
        </div>
        <RouterLink to="/author/posts/new">
          <BaseButton>写文章</BaseButton>
        </RouterLink>
      </div>

      <p v-if="error" class="author-error">{{ error }}</p>
      <LoadingState v-if="loading" />
      <EmptyState v-else-if="!posts.length" message="还没有文章，开始写第一篇吧" />
      <ul v-else class="author-list">
        <li v-for="post in posts" :key="post.id" class="author-item">
          <div class="author-item__main">
            <RouterLink :to="`/author/posts/${post.id}/edit`" class="author-item__title">
              {{ post.title || '无标题' }}
            </RouterLink>
            <div class="author-item__meta">
              <span class="status" :class="`status--${post.status}`">
                {{ post.status === 'published' ? '已发布' : '草稿' }}
              </span>
              <span v-if="post.publishedAt" class="text-muted">{{ formatDate(post.publishedAt) }}</span>
              <span class="text-muted">/{{ post.slug }}</span>
            </div>
          </div>
          <div class="author-item__actions">
            <RouterLink :to="`/author/posts/${post.id}/edit`">
              <BaseButton size="sm" variant="secondary" :disabled="acting">编辑</BaseButton>
            </RouterLink>
            <BaseButton
              v-if="post.status === 'draft'"
              size="sm"
              type="button"
              :disabled="acting"
              @click="onPublish(post.id)"
            >
              发布
            </BaseButton>
            <BaseButton
              v-else
              size="sm"
              variant="secondary"
              type="button"
              :disabled="acting"
              @click="onUnpublish(post.id)"
            >
              取消发布
            </BaseButton>
            <BaseButton
              size="sm"
              variant="secondary"
              type="button"
              :disabled="acting"
              @click="onRemove(post.id)"
            >
              删除
            </BaseButton>
          </div>
        </li>
      </ul>
    </PageContainer>
  </section>
</template>

<style scoped>
.author-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.author-title {
  font-family: var(--font-display);
  font-size: clamp(1.75rem, 4vw, 2.25rem);
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.author-error {
  color: var(--color-cinnabar, #b83b2d);
  margin-bottom: var(--spacing-md);
}

.author-list {
  list-style: none;
  border-top: 1px solid var(--color-line, var(--color-border));
}

.author-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-lg);
  padding: 1.25rem 0;
  border-bottom: 1px solid var(--color-line, var(--color-border));
}

.author-item__title {
  font-family: var(--font-display);
  font-size: 1.125rem;
  font-weight: 600;
}

.author-item__title:hover {
  color: var(--color-cinnabar, var(--color-brand-ink));
}

.author-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 0.35rem;
  font-size: 0.875rem;
}

.status {
  font-weight: 600;
}

.status--draft {
  color: var(--color-mist, var(--color-text-muted));
}

.status--published {
  color: var(--color-cinnabar, var(--color-brand));
}

.author-item__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  flex-shrink: 0;
}

@media (max-width: 720px) {
  .author-head,
  .author-item {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
