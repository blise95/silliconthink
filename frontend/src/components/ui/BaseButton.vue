<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary'
  size?: 'sm' | 'md' | 'lg'
  type?: 'button' | 'submit'
  disabled?: boolean
}>()
</script>

<template>
  <button
    class="btn"
    :class="[
      variant === 'secondary' ? 'btn--secondary' : 'btn--primary',
      size === 'lg' ? 'btn--lg' : size === 'sm' ? 'btn--sm' : '',
    ]"
    :type="type ?? 'button'"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>
