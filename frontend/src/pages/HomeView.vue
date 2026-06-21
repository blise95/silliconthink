<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import PostCard from '@/components/blog/PostCard.vue'
import ProjectCard from '@/components/project/ProjectCard.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import { useSiteConfig } from '@/composables/useSiteConfig'
import { usePageMeta } from '@/composables/usePageMeta'
import { useScrollReveal } from '@/composables/useScrollReveal'
import { postRepo } from '@/services'
import { useFeaturedProjects } from '@/composables/useProjects'
import type { Post } from '@/types'

const pageRef = ref<HTMLElement | null>(null)
useScrollReveal(pageRef)

const { site, loading: siteLoading } = useSiteConfig()
const { projects, loading: projectsLoading } = useFeaturedProjects(3)
const latestPosts = ref<Post[]>([])
const postsLoading = ref(true)

usePageMeta({
  title: computed(() => site.value?.siteName ?? 'Silicon Think'),
  description: computed(() => site.value?.seoDescription),
  site,
})

onMounted(async () => {
  latestPosts.value = await postRepo.getLatest(3)
  postsLoading.value = false
})
</script>

<template>
  <div ref="pageRef">
    <section class="hero section">
      <PageContainer>
        <div v-if="siteLoading" class="text-center">
          <LoadingState />
        </div>
        <div v-else-if="site" class="hero__content text-center animate-on-scroll">
          <p class="hero__eyebrow">{{ site.siteName }}</p>
          <h1 class="hero__title">{{ site.tagline }}</h1>
          <p class="hero__desc text-muted mt-lg">
            分享前端工程、后端架构与技术实践的个人站点
          </p>
          <div class="hero__actions mt-xl">
            <RouterLink to="/projects">
              <BaseButton size="lg">看项目</BaseButton>
            </RouterLink>
            <RouterLink to="/blog">
              <BaseButton variant="secondary" size="lg">读博客</BaseButton>
            </RouterLink>
          </div>
        </div>
      </PageContainer>
    </section>

    <section class="section">
      <PageContainer>
        <SectionTitle title="精选项目" subtitle="最近在做的一些东西" />
        <LoadingState v-if="projectsLoading" />
        <div v-else class="grid-3">
          <div
            v-for="(project, index) in projects"
            :key="project.id"
            class="animate-on-scroll"
            :style="{ transitionDelay: `${index * 80}ms` }"
          >
            <ProjectCard :project="project" />
          </div>
        </div>
      </PageContainer>
    </section>

    <section class="section" style="background: var(--color-surface-muted)">
      <PageContainer>
        <SectionTitle title="最新博客" subtitle="技术笔记与思考" />
        <LoadingState v-if="postsLoading" />
        <div v-else class="grid-3">
          <div
            v-for="(post, index) in latestPosts"
            :key="post.id"
            class="animate-on-scroll"
            :style="{ transitionDelay: `${index * 80}ms` }"
          >
            <PostCard :post="post" />
          </div>
        </div>
      </PageContainer>
    </section>

    <section v-if="site" class="section">
      <PageContainer>
        <SectionTitle title="技能栈" centered />
        <div class="tag-list animate-on-scroll" style="justify-content: center">
          <span v-for="skill in site.skills" :key="skill" class="tag">{{ skill }}</span>
        </div>
      </PageContainer>
    </section>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(180deg, var(--color-surface-muted) 0%, var(--color-surface) 100%);
}

.hero__eyebrow {
  color: var(--color-brand-ink);
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  font-size: 0.875rem;
}

.hero__title {
  font-family: var(--font-display);
  font-size: clamp(2rem, 5vw, 3rem);
  line-height: 1.15;
  max-width: 16ch;
  margin-inline: auto;
}

.hero__desc {
  max-width: 42ch;
  margin-inline: auto;
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  justify-content: center;
}
</style>
