<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '@/components/ui/PageContainer.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import { useAuth } from '@/composables/useAuth'
import { usePageMeta } from '@/composables/usePageMeta'
import { ApiError } from '@/utils/http'

usePageMeta({ title: 'OAuth 回调', description: '完成第三方登录' })

const route = useRoute()
const router = useRouter()
const { exchangeOAuthCode } = useAuth()
const message = ref('正在完成登录…')

onMounted(async () => {
  const err = route.query.error
  if (err) {
    message.value = String(err)
    return
  }
  const code = route.query.code
  if (typeof code !== 'string' || !code) {
    message.value = '缺少授权码'
    return
  }
  try {
    await exchangeOAuthCode(code)
    await router.replace('/')
  } catch (e) {
    message.value = e instanceof ApiError || e instanceof Error ? e.message : '换票失败'
  }
})
</script>

<template>
  <PageContainer>
    <section class="auth">
      <SectionTitle title="GitHub 登录" :subtitle="message" />
    </section>
  </PageContainer>
</template>

<style scoped>
.auth {
  max-width: 420px;
  margin: 0 auto;
  padding: var(--spacing-2xl) 0;
}
</style>
