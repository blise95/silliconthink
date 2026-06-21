<script setup lang="ts">
defineProps<{
  href?: string
  tag?: 'div' | 'article'
}>()
</script>

<template>
  <component
    :is="href ? 'a' : tag ?? 'div'"
    class="card"
    :href="href"
  >
    <slot name="cover" />
    <div class="card__body">
      <slot />
    </div>
  </component>
</template>
