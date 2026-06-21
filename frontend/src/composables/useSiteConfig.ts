import { ref, onMounted } from 'vue'
import { siteRepo } from '@/services'
import type { SiteConfig } from '@/types'

const cached = ref<SiteConfig | null>(null)

export function useSiteConfig() {
  const site = ref<SiteConfig | null>(cached.value)
  const loading = ref(!cached.value)
  const error = ref<string | null>(null)

  onMounted(async () => {
    if (cached.value) {
      site.value = cached.value
      loading.value = false
      return
    }
    try {
      cached.value = await siteRepo.getSiteConfig()
      site.value = cached.value
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  })

  return { site, loading, error }
}
