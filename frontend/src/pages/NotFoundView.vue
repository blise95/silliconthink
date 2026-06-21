<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseButton from '@/components/ui/BaseButton.vue'
</script>

<template>
  <div class="page-state">
    <h1 style="font-family: var(--font-display); margin-bottom: 1rem">404</h1>
    <p class="mb-lg">页面不存在</p>
    <RouterLink to="/">
      <BaseButton>返回首页</BaseButton>
    </RouterLink>
  </div>
</template>
