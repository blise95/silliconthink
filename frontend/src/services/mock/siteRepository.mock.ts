import { siteMock } from '@/mocks/site'
import type { SiteRepository } from '@/services/siteRepository'

export const mockSiteRepository: SiteRepository = {
  async getSiteConfig() {
    return { ...siteMock }
  },
}
