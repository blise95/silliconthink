import { computed, type Ref } from 'vue'
import { useHead } from '@unhead/vue'
import type { SiteConfig } from '@/types'

export function usePageMeta(options: {
  title: Ref<string | undefined> | string
  description?: Ref<string | undefined> | string
  image?: Ref<string | undefined> | string
  site?: Ref<SiteConfig | null | undefined>
}) {
  const resolvedTitle = computed(() => {
    const t = typeof options.title === 'string' ? options.title : options.title.value
    const siteName = options.site?.value?.siteName ?? 'Silicon Think'
    if (!t || t === siteName) return siteName
    return `${t} | ${siteName}`
  })

  const resolvedDescription = computed(() => {
    if (typeof options.description === 'string') return options.description
    return (
      options.description?.value ??
      options.site?.value?.seoDescription ??
      ''
    )
  })

  const resolvedImage = computed(() => {
    if (typeof options.image === 'string') return options.image
    return options.image?.value ?? options.site?.value?.ogImage ?? '/og-default.svg'
  })

  useHead({
    title: resolvedTitle,
    meta: computed(() => [
      { name: 'description', content: resolvedDescription.value },
      { property: 'og:title', content: resolvedTitle.value },
      { property: 'og:description', content: resolvedDescription.value },
      { property: 'og:image', content: resolvedImage.value },
      { property: 'og:type', content: 'website' },
    ]),
  })
}
