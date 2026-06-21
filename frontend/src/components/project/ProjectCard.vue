<script setup lang="ts">
import BaseCard from '@/components/ui/BaseCard.vue'
import type { Project } from '@/types'

defineProps<{ project: Project }>()
</script>

<template>
  <BaseCard :href="`/projects/${project.slug}`" tag="article">
    <template v-if="project.coverUrl" #cover>
      <img
        class="card__cover"
        :src="project.coverUrl"
        :alt="project.title"
        loading="lazy"
      />
    </template>
    <span class="tag">{{ project.category }}</span>
    <h3 style="font-family: var(--font-display); margin: 0.75rem 0 0.5rem">
      {{ project.title }}
    </h3>
    <p class="text-muted">{{ project.summary }}</p>
    <div class="tag-list mt-lg">
      <span v-for="tech in project.techStack" :key="tech" class="tag">{{ tech }}</span>
    </div>
  </BaseCard>
</template>
