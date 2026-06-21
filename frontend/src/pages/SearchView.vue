<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import PostCard from '@/components/blog/PostCard.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { usePosts } from '@/composables/usePosts'
import { usePageMeta } from '@/composables/usePageMeta'

const route = useRoute()
const keyword = computed(() => (typeof route.query.q === 'string' ? route.query.q : ''))

const params = computed(() => ({
  keyword: keyword.value,
  pageSize: 20,
}))

const { data, loading } = usePosts(params)

usePageMeta({
  title: computed(() => (keyword.value ? `搜索：${keyword.value}` : '搜索')),
  description: '搜索博客文章',
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <SectionTitle
        :title="keyword ? `搜索：${keyword}` : '搜索'"
        subtitle="按标题或摘要匹配"
      />

      <form class="search-form mb-xl" action="/search" method="get">
        <input
          name="q"
          type="search"
          :value="keyword"
          placeholder="输入关键词..."
          aria-label="搜索关键词"
        />
        <button class="btn btn--primary" type="submit">搜索</button>
      </form>

      <LoadingState v-if="loading" />
      <EmptyState v-else-if="!keyword" message="请输入关键词开始搜索" />
      <EmptyState v-else-if="!data?.list.length" message="未找到匹配文章" />
      <div v-else class="grid-2">
        <PostCard v-for="post in data.list" :key="post.id" :post="post" />
      </div>

      <p v-if="keyword && data?.list.length" class="text-muted mt-lg">
        共 {{ data.total }} 条结果 ·
        <RouterLink to="/blog" style="color: var(--color-brand-ink)">浏览全部博客</RouterLink>
      </p>
    </PageContainer>
  </section>
</template>

<style scoped>
.search-form {
  display: flex;
  gap: var(--spacing-md);
  max-width: 560px;
}

.search-form input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
</style>
