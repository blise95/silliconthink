<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseCard from '@/components/ui/BaseCard.vue'
import type { Post } from '@/types'
import { formatDate, tagLabel } from '@/utils/markdown'

defineProps<{ post: Post }>()
</script>

<template>
  <BaseCard :href="`/blog/${post.slug}`" tag="article">
    <template v-if="post.coverUrl" #cover>
      <img class="card__cover" :src="post.coverUrl" :alt="post.title" loading="lazy" />
    </template>
    <time class="text-muted" :datetime="post.publishedAt">{{ formatDate(post.publishedAt) }}</time>
    <h3 style="font-family: var(--font-display); margin: 0.5rem 0">{{ post.title }}</h3>
    <p class="text-muted">{{ post.summary }}</p>
    <div class="tag-list mt-lg">
      <RouterLink
        v-for="tag in post.tags"
        :key="tag"
        :to="{ path: '/blog', query: { tag } }"
        class="tag"
        @click.stop
      >
        {{ tagLabel(tag) }}
      </RouterLink>
    </div>
  </BaseCard>
</template>
