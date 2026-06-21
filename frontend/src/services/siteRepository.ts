import type { SiteConfig } from '@/types'

export interface SiteRepository {
  getSiteConfig(): Promise<SiteConfig>
}
