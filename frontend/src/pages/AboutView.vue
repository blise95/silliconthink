<script setup lang="ts">
import PageContainer from '@/components/ui/PageContainer.vue'
import MarkdownViewer from '@/components/blog/MarkdownViewer.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import { useSiteConfig } from '@/composables/useSiteConfig'
import { usePageMeta } from '@/composables/usePageMeta'
import { Github, Mail } from 'lucide-vue-next'

const { site, loading } = useSiteConfig()

usePageMeta({
  title: '关于',
  description: '关于 Silicon Think',
  site,
})
</script>

<template>
  <section class="section">
    <PageContainer>
      <LoadingState v-if="loading" />
      <div v-else-if="site" class="about">
        <h1 class="about__title">关于我</h1>
        <MarkdownViewer :content="site.aboutMd" />

        <h2 class="about__subtitle">技能</h2>
        <div class="tag-list mb-xl">
          <span v-for="skill in site.skills" :key="skill" class="tag">{{ skill }}</span>
        </div>

        <h2 class="about__subtitle">联系方式</h2>
        <div class="about__contact">
          <a
            v-for="item in site.contact"
            :key="item.url"
            :href="item.url"
            class="about__contact-link"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Github v-if="item.icon === 'github'" :size="18" />
            <Mail v-else :size="18" />
            {{ item.label }}
          </a>
        </div>
      </div>
    </PageContainer>
  </section>
</template>

<style scoped>
.about {
  max-width: 760px;
  margin-inline: auto;
}

.about__title {
  font-family: var(--font-display);
  font-size: clamp(1.75rem, 4vw, 2.5rem);
  margin-bottom: var(--spacing-xl);
}

.about__subtitle {
  font-family: var(--font-display);
  margin: var(--spacing-2xl) 0 var(--spacing-md);
}

.about__contact {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.about__contact-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0.625rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.about__contact-link:hover {
  border-color: var(--color-brand);
  color: var(--color-brand-ink);
}
</style>
