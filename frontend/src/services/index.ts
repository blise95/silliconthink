import type { AuthRepository } from '@/services/authRepository'
import type { PostRepository } from '@/services/postRepository'
import type { ProjectRepository } from '@/services/projectRepository'
import type { SiteRepository } from '@/services/siteRepository'
import { apiAuthRepository } from '@/services/api/authRepository.api'
import { mockAuthRepository } from '@/services/mock/authRepository.mock'
import { mockPostRepository } from '@/services/mock/postRepository.mock'
import { mockProjectRepository } from '@/services/mock/projectRepository.mock'
import { mockSiteRepository } from '@/services/mock/siteRepository.mock'
import { isAuthApiEnabled } from '@/utils/http'

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

/** Auth can use real API while content stays on Mock */
export const authRepo: AuthRepository = isAuthApiEnabled()
  ? apiAuthRepository
  : mockAuthRepository
