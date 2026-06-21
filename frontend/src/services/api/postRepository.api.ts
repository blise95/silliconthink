import type { PostRepository } from '@/services/postRepository'

/** Placeholder — swap in when Spring Boot API is ready */
export const apiPostRepository: PostRepository = {
  async list() {
    throw new Error('API repository not implemented')
  },
  async getBySlug() {
    throw new Error('API repository not implemented')
  },
  async getLatest() {
    throw new Error('API repository not implemented')
  },
}
