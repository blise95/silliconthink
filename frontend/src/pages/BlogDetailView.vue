<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import MarkdownViewer from '@/components/blog/MarkdownViewer.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import NotFoundView from '@/pages/NotFoundView.vue'
import { usePost } from '@/composables/usePosts'
import { usePageMeta } from '@/composables/usePageMeta'
import { formatDate, tagLabel } from '@/utils/markdown'

const route = useRoute()
const slug = computed(() => route.params.slug as string)
const { post, loading, error } = usePost(slug)

usePageMeta({
  title: computed(() => post.value?.title),
  description: computed(() => post.value?.summary),
  image: computed(() => post.value?.coverUrl),
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <LoadingState v-if="loading" />
      <p v-else-if="error" class="text-muted">{{ error }}</p>
      <NotFoundView v-else-if="!post" />
      <article v-else class="article">
        <header class="article__header">
          <h1 class="article__title">{{ post.title }}</h1>
          <div class="article__meta">
            <time v-if="post.publishedAt" :datetime="post.publishedAt">
              {{ formatDate(post.publishedAt) }}
            </time>
            <span v-if="post.authorDisplayName" class="article__author">
              {{ post.authorDisplayName }}
            </span>
            <span v-for="tag in post.tags" :key="tag" class="tag">{{ tagLabel(tag) }}</span>
          </div>
        </header>
        <img
          v-if="post.coverUrl"
          :src="post.coverUrl"
          :alt="post.title"
          style="width: 100%; border-radius: var(--radius-lg); margin-bottom: 2rem"
        />
        <MarkdownViewer :content="post.contentMd" />
      </article>
    </PageContainer>
  </section>
</template>
