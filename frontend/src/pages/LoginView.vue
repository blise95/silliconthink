<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import { useAuth } from '@/composables/useAuth'
import { usePageMeta } from '@/composables/usePageMeta'
import { ApiError } from '@/utils/http'

usePageMeta({ title: '登录', description: '登录 Silicon Think' })

const router = useRouter()
const route = useRoute()
const { login, startGitHubOAuth, authEnabled, loading } = useAuth()

const username = ref('')
const password = ref('')
const error = ref('')

onMounted(() => {
  if (route.query.error) {
    error.value = String(route.query.error)
  }
})

async function onSubmit() {
  error.value = ''
  try {
    await login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect || '/')
  } catch (e) {
    error.value = e instanceof ApiError || e instanceof Error ? e.message : '登录失败'
  }
}
</script>

<template>
  <PageContainer>
    <section class="auth">
      <SectionTitle title="登录" subtitle="使用账号密码或 GitHub 继续" />
      <form class="auth__form" @submit.prevent="onSubmit">
        <label class="auth__field">
          <span>用户名</span>
          <input v-model="username" name="username" autocomplete="username" required />
        </label>
        <label class="auth__field">
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            name="password"
            autocomplete="current-password"
            required
          />
        </label>
        <p v-if="error" class="auth__error">{{ error }}</p>
        <BaseButton type="submit" :disabled="loading">登录</BaseButton>
        <BaseButton
          v-if="authEnabled"
          type="button"
          variant="secondary"
          :disabled="loading"
          @click="startGitHubOAuth"
        >
          使用 GitHub 登录
        </BaseButton>
        <p class="auth__hint">
          还没有账号？
          <RouterLink to="/register">去注册</RouterLink>
        </p>
        <p v-if="!authEnabled" class="auth__hint">
          当前为 Mock 认证（demo / demo1234）。联调请设置
          <code>VITE_AUTH_USE_API=true</code>。
        </p>
      </form>
    </section>
  </PageContainer>
</template>

<style scoped>
.auth {
  max-width: 420px;
  margin: 0 auto;
  padding: var(--spacing-2xl) 0;
}

.auth__form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
}

.auth__field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  font-weight: 500;
}

.auth__field input {
  padding: 0.75rem 0.9rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}

.auth__error {
  color: #b91c1c;
  margin: 0;
}

.auth__hint {
  color: var(--color-text-muted);
  margin: 0;
}

.auth__hint code {
  font-family: var(--font-mono);
  font-size: 0.85em;
}
</style>
