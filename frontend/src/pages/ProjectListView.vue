<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import ProjectCard from '@/components/project/ProjectCard.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { useProjects } from '@/composables/useProjects'
import { usePageMeta } from '@/composables/usePageMeta'
import { projectsMock } from '@/mocks/projects'

const route = useRoute()

const categories = computed(() => {
  const set = new Set(
    projectsMock.filter((p) => p.status === 'published').map((p) => p.category),
  )
  return Array.from(set)
})

const params = computed(() => ({
  category:
    typeof route.query.category === 'string' ? route.query.category : undefined,
}))

const { data, loading } = useProjects(params)

usePageMeta({
  title: '项目',
  description: '个人项目与作品展示',
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <SectionTitle title="项目" subtitle="开源练习与 side project" />

      <div class="tag-list mb-xl">
        <RouterLink
          :to="{ path: '/projects' }"
          class="tag"
          :class="{ 'tag--active': !route.query.category }"
        >
          全部
        </RouterLink>
        <RouterLink
          v-for="category in categories"
          :key="category"
          :to="{ path: '/projects', query: { category } }"
          class="tag"
          :class="{ 'tag--active': route.query.category === category }"
        >
          {{ category }}
        </RouterLink>
      </div>

      <LoadingState v-if="loading" />
      <EmptyState v-else-if="!data?.list.length" message="该分类下暂无项目" />
      <div v-else class="grid-3">
        <ProjectCard v-for="project in data.list" :key="project.id" :project="project" />
      </div>
    </PageContainer>
  </section>
</template>
