<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import MarkdownViewer from '@/components/blog/MarkdownViewer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import NotFoundView from '@/pages/NotFoundView.vue'
import { useProject } from '@/composables/useProjects'
import { usePageMeta } from '@/composables/usePageMeta'
import { ExternalLink, Github } from 'lucide-vue-next'

const route = useRoute()
const slug = computed(() => route.params.slug as string)
const { project, loading } = useProject(slug)

usePageMeta({
  title: computed(() => project.value?.title),
  description: computed(() => project.value?.summary),
  image: computed(() => project.value?.coverUrl),
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <LoadingState v-if="loading" />
      <NotFoundView v-else-if="!project" />
      <article v-else class="article">
        <header class="article__header">
          <span class="tag">{{ project.category }}</span>
          <h1 class="article__title">{{ project.title }}</h1>
          <p class="text-muted">{{ project.summary }}</p>
          <div class="tag-list mt-lg">
            <span v-for="tech in project.techStack" :key="tech" class="tag">{{ tech }}</span>
          </div>
          <div class="project-actions mt-lg">
            <a
              v-if="project.demoUrl"
              :href="project.demoUrl"
              target="_blank"
              rel="noopener noreferrer"
            >
              <BaseButton>
                <ExternalLink :size="16" />
                在线演示
              </BaseButton>
            </a>
            <a
              v-if="project.repoUrl"
              :href="project.repoUrl"
              target="_blank"
              rel="noopener noreferrer"
            >
              <BaseButton variant="secondary">
                <Github :size="16" />
                源码
              </BaseButton>
            </a>
          </div>
        </header>

        <img
          v-if="project.coverUrl"
          :src="project.coverUrl"
          :alt="project.title"
          style="width: 100%; border-radius: var(--radius-lg); margin-bottom: 2rem"
        />

        <MarkdownViewer :content="project.contentMd" />
      </article>
    </PageContainer>
  </section>
</template>

<style scoped>
.project-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}
</style>
