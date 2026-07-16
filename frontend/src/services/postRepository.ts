import type { MyPostListParams, Paginated, Post, PostListParams, PostWritePayload } from '@/types'

export interface PostRepository {
  list(params?: PostListParams): Promise<Paginated<Post>>
  getBySlug(slug: string): Promise<Post | null>
  getLatest(count: number): Promise<Post[]>
  listMine(params?: MyPostListParams): Promise<Paginated<Post>>
  getMineById(id: string): Promise<Post>
  create(payload: PostWritePayload): Promise<Post>
  update(id: string, payload: PostWritePayload): Promise<Post>
  publish(id: string): Promise<Post>
  unpublish(id: string): Promise<Post>
  remove(id: string): Promise<void>
  uploadImage(file: File): Promise<string>
}
