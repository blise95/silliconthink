import { postsMock } from '@/mocks/posts'
import type { PostRepository } from '@/services/postRepository'
import type { Paginated, Post, PostListParams } from '@/types'

function publishedPosts(): Post[] {
  return postsMock
    .filter((p) => p.status === 'published')
    .sort(
      (a, b) =>
        new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime(),
    )
}

function filterPosts(posts: Post[], params: PostListParams = {}): Post[] {
  let result = [...posts]

  if (params.tag) {
    result = result.filter((p) => p.tags.includes(params.tag!))
  }

  if (params.keyword?.trim()) {
    const kw = params.keyword.trim().toLowerCase()
    result = result.filter(
      (p) =>
        p.title.toLowerCase().includes(kw) ||
        p.summary.toLowerCase().includes(kw),
    )
  }

  return result
}

function paginate<T>(
  items: T[],
  page = 1,
  pageSize = 10,
): Paginated<T> {
  const start = (page - 1) * pageSize
  return {
    list: items.slice(start, start + pageSize),
    total: items.length,
    page,
    pageSize,
  }
}

export const mockPostRepository: PostRepository = {
  async list(params = {}) {
    const filtered = filterPosts(publishedPosts(), params)
    return paginate(filtered, params.page ?? 1, params.pageSize ?? 10)
  },

  async getBySlug(slug) {
    return publishedPosts().find((p) => p.slug === slug) ?? null
  },

  async getLatest(count) {
    return publishedPosts().slice(0, count)
  },
}
