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
import { tagsMock } from '@/mocks/tags'
import { tagLabel } from '@/utils/markdown'

const route = useRoute()

const params = computed(() => ({
  page: Number(route.query.page ?? 1),
  pageSize: 10,
  tag: typeof route.query.tag === 'string' ? route.query.tag : undefined,
}))

const { data, loading, error } = usePosts(params)
const totalPages = computed(() =>
  data.value ? Math.max(1, Math.ceil(data.value.total / data.value.pageSize)) : 1,
)

usePageMeta({
  title: '博客',
  description: '技术博客与工程实践',
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <SectionTitle title="博客" subtitle="文章、笔记与思考" />

      <div class="tag-list mb-xl">
        <RouterLink
          :to="{ path: '/blog' }"
          class="tag"
          :class="{ 'tag--active': !route.query.tag }"
        >
          全部
        </RouterLink>
        <RouterLink
          v-for="tag in tagsMock"
          :key="tag.id"
          :to="{ path: '/blog', query: { tag: tag.slug } }"
          class="tag"
          :class="{ 'tag--active': route.query.tag === tag.slug }"
        >
          {{ tagLabel(tag.slug) }}
        </RouterLink>
      </div>

      <LoadingState v-if="loading" />
      <EmptyState
        v-else-if="error"
        :message="`加载失败：${error}`"
      />
      <EmptyState v-else-if="!data?.list.length" message="没有找到文章" />
      <div v-else class="grid-2">
        <PostCard v-for="post in data.list" :key="post.id" :post="post" />
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <RouterLink
          v-for="page in totalPages"
          :key="page"
          :to="{ path: '/blog', query: { ...route.query, page: page === 1 ? undefined : page } }"
          class="pagination__btn"
          :class="{ 'is-active': page === params.page }"
        >
          {{ page }}
        </RouterLink>
      </div>
    </PageContainer>
  </section>
</template>
