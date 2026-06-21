<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  centered?: boolean
}>()
</script>

<template>
  <div class="section-title" :class="{ 'text-center': centered }">
    {{ title }}
    <span v-if="subtitle" class="section-title__subtitle">{{ subtitle }}</span>
  </div>
</template>
