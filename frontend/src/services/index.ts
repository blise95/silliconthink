import type { PostRepository } from '@/services/postRepository'
import type { ProjectRepository } from '@/services/projectRepository'
import type { SiteRepository } from '@/services/siteRepository'
import { mockPostRepository } from '@/services/mock/postRepository.mock'
import { mockProjectRepository } from '@/services/mock/projectRepository.mock'
import { mockSiteRepository } from '@/services/mock/siteRepository.mock'

const useMock = import.meta.env.VITE_USE_MOCK !== 'false'

// Placeholder for future API implementations
// import { apiPostRepository } from '@/services/api/postRepository.api'

export const postRepo: PostRepository = useMock
  ? mockPostRepository
  : mockPostRepository

export const projectRepo: ProjectRepository = useMock
  ? mockProjectRepository
  : mockProjectRepository

export const siteRepo: SiteRepository = useMock
  ? mockSiteRepository
  : mockSiteRepository
