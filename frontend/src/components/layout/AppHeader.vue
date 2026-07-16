<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'
import BaseButton from '@/components/ui/BaseButton.vue'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)
const { user, isLoggedIn, logout, bootstrap } = useAuth()

onMounted(() => {
  void bootstrap()
})

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

async function onLogout() {
  await logout()
  closeMenu()
  if (route.path.startsWith('/login') || route.path.startsWith('/register')) {
    await router.push('/')
  }
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

        <template v-if="isLoggedIn">
          <RouterLink
            to="/author/posts"
            class="header__link"
            :class="{ 'header__link--active': isActive('/author') }"
            @click="closeMenu"
          >
            我的文章
          </RouterLink>
          <RouterLink to="/author/posts/new" @click="closeMenu">
            <BaseButton size="sm">写文章</BaseButton>
          </RouterLink>
          <span class="header__user">{{ user?.displayName || user?.username }}</span>
          <BaseButton size="sm" variant="secondary" type="button" @click="onLogout">
            登出
          </BaseButton>
        </template>
        <RouterLink v-else to="/login" @click="closeMenu">
          <BaseButton size="sm" variant="secondary">登录</BaseButton>
        </RouterLink>

        <RouterLink v-if="!isLoggedIn" to="/projects" @click="closeMenu">
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
  background: rgba(245, 245, 247, 0.88);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-line);
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
  width: 1.75rem;
  height: 1.75rem;
  border-radius: var(--radius-sm);
  background: var(--color-ink);
  color: #fff;
  font-family: var(--font-display);
  font-size: 0.7rem;
  position: relative;
}

.header__logo-mark::after {
  content: '';
  position: absolute;
  right: 3px;
  bottom: 3px;
  width: 4px;
  height: 4px;
  border-radius: 1px;
  background: var(--color-cinnabar);
}

.header__logo-text {
  font-family: var(--font-display);
  letter-spacing: 0.04em;
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
  color: var(--color-mist);
  font-weight: 500;
}

.header__link--active,
.header__link:hover {
  color: var(--color-ink);
}

.header__search input {
  width: 160px;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}

.header__user {
  color: var(--color-ink-soft);
  font-weight: 600;
  font-size: 0.9rem;
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
    border-bottom: 1px solid var(--color-line);
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
