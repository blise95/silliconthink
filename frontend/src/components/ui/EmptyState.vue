<script setup lang="ts">
defineProps<{ message?: string }>()
</script>

<template>
  <div class="page-state">{{ message ?? '暂无内容' }}</div>
</template>
