<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import { useAuth } from '@/composables/useAuth'
import { usePageMeta } from '@/composables/usePageMeta'
import { ApiError } from '@/utils/http'

usePageMeta({ title: '注册', description: '注册 Silicon Think 账号' })

const router = useRouter()
const { register, loading } = useAuth()

const username = ref('')
const displayName = ref('')
const password = ref('')
const error = ref('')

async function onSubmit() {
  error.value = ''
  try {
    await register(username.value.trim(), password.value, displayName.value.trim() || undefined)
    await router.replace('/')
  } catch (e) {
    error.value = e instanceof ApiError || e instanceof Error ? e.message : '注册失败'
  }
}
</script>

<template>
  <PageContainer>
    <section class="auth">
      <SectionTitle title="注册" subtitle="创建账号后即可登录" />
      <form class="auth__form" @submit.prevent="onSubmit">
        <label class="auth__field">
          <span>用户名</span>
          <input
            v-model="username"
            name="username"
            autocomplete="username"
            required
            minlength="4"
            maxlength="32"
            pattern="[A-Za-z0-9_]+"
          />
        </label>
        <label class="auth__field">
          <span>展示名（可选）</span>
          <input v-model="displayName" name="displayName" maxlength="64" />
        </label>
        <label class="auth__field">
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            name="password"
            autocomplete="new-password"
            required
            minlength="8"
          />
        </label>
        <p class="auth__hint">密码至少 8 位，需同时包含字母与数字。</p>
        <p v-if="error" class="auth__error">{{ error }}</p>
        <BaseButton type="submit" :disabled="loading">注册并登录</BaseButton>
        <p class="auth__hint">
          已有账号？
          <RouterLink to="/login">去登录</RouterLink>
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
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}

.auth__error {
  color: var(--color-cinnabar);
  margin: 0;
}

.auth__hint {
  color: var(--color-text-muted);
  margin: 0;
}
</style>
