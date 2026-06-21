<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'
import BaseButton from '@/components/ui/BaseButton.vue'

const route = useRoute()
const menuOpen = ref(false)

const links = [
  { to: '/', label: '首页' },
  { to: '/about', label: '关于' },
  { to: '/blog', label: '博客' },
  { to: '/projects', label: '项目' },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function closeMenu() {
  menuOpen.value = false
}
</script>

<template>
  <header class="header">
    <div class="container header__inner">
      <RouterLink to="/" class="header__logo" @click="closeMenu">
        <span class="header__logo-mark">ST</span>
        <span class="header__logo-text">Silicon Think</span>
      </RouterLink>

      <button
        class="header__toggle"
        type="button"
        aria-label="菜单"
        @click="menuOpen = !menuOpen"
      >
        <Menu v-if="!menuOpen" :size="22" />
        <X v-else :size="22" />
      </button>

      <nav class="header__nav" :class="{ 'header__nav--open': menuOpen }">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="header__link"
          :class="{ 'header__link--active': isActive(link.to) }"
          @click="closeMenu"
        >
          {{ link.label }}
        </RouterLink>

        <form class="header__search" action="/search" method="get" @submit="closeMenu">
          <input
            name="q"
            type="search"
            placeholder="搜索文章..."
            aria-label="搜索文章"
          />
        </form>

        <RouterLink to="/projects" @click="closeMenu">
          <BaseButton size="sm">看项目</BaseButton>
        </RouterLink>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--color-border);
}

.header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header__logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
}

.header__logo-mark {
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border-radius: var(--radius-md);
  background: var(--color-brand);
  color: #fff;
  font-family: var(--font-display);
  font-size: 0.75rem;
}

.header__toggle {
  display: none;
  border: none;
  background: transparent;
  cursor: pointer;
}

.header__nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.header__link {
  color: var(--color-text-muted);
  font-weight: 500;
}

.header__link--active,
.header__link:hover {
  color: var(--color-brand-ink);
}

.header__search input {
  width: 160px;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}

@media (max-width: 768px) {
  .header__toggle {
    display: inline-flex;
  }

  .header__nav {
    position: absolute;
    top: var(--header-height);
    left: 0;
    right: 0;
    flex-direction: column;
    align-items: stretch;
    padding: var(--spacing-lg);
    background: var(--color-surface);
    border-bottom: 1px solid var(--color-border);
    display: none;
  }

  .header__nav--open {
    display: flex;
  }

  .header__search input {
    width: 100%;
  }
}
</style>
