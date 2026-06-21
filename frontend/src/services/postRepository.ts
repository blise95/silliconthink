import type { Paginated, Post, PostListParams } from '@/types'

export interface PostRepository {
  list(params?: PostListParams): Promise<Paginated<Post>>
  getBySlug(slug: string): Promise<Post | null>
  getLatest(count: number): Promise<Post[]>
}
