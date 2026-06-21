<script setup lang="ts">
import { Github, Mail } from 'lucide-vue-next'
</script>

<template>
  <footer class="footer">
    <div class="container footer__inner">
      <p class="footer__copy">© {{ new Date().getFullYear() }} Silicon Think</p>
      <div class="footer__links">
        <a href="https://github.com" target="_blank" rel="noopener noreferrer" aria-label="GitHub">
          <Github :size="18" />
        </a>
        <a href="mailto:hello@example.com" aria-label="Email">
          <Mail :size="18" />
        </a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  border-top: 1px solid var(--color-border);
  padding: var(--spacing-xl) 0;
  margin-top: auto;
}

.footer__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
  color: var(--color-text-muted);
  font-size: 0.9375rem;
}

.footer__links {
  display: flex;
  gap: var(--spacing-md);
}

.footer__links a:hover {
  color: var(--color-brand-ink);
}
</style>
