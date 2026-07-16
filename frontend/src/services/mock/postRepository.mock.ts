import { postsMock } from '@/mocks/posts'
import type { PostRepository } from '@/services/postRepository'
import type { MyPostListParams, Paginated, Post, PostListParams, PostWritePayload, PublishStatus } from '@/types'
import type { AuthUserInfo } from '@/types/auth'
import { getStoredToken } from '@/utils/http'
import { getUserSnapshot } from '@/services/mock/userSnapshot'

/** Mutable in-memory store seeded from mock data */
const store: Post[] = postsMock.map((p) => ({
  ...p,
  authorId: p.authorId ?? '1',
  authorDisplayName: p.authorDisplayName ?? 'Demo User',
}))

let seq = store.reduce((max, p) => Math.max(max, Number(p.id) || 0), 0)

function requireUser(): AuthUserInfo {
  if (!getStoredToken()) {
    throw new Error('unauthorized')
  }
  const user = getUserSnapshot()
  if (!user) {
    throw new Error('unauthorized')
  }
  return user
}

function requireUserId(): string {
  return String(requireUser().id)
}

function publishedPosts(): Post[] {
  return store
    .filter((p) => p.status === 'published')
    .sort(
      (a, b) =>
        new Date(b.publishedAt || 0).getTime() - new Date(a.publishedAt || 0).getTime(),
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

function paginate<T>(items: T[], page = 1, pageSize = 10): Paginated<T> {
  const start = (page - 1) * pageSize
  return {
    list: items.slice(start, start + pageSize),
    total: items.length,
    page,
    pageSize,
  }
}

function assertSlugAvailable(slug: string, excludeId?: string) {
  const hit = store.find((p) => p.slug === slug && p.id !== excludeId)
  if (hit) {
    throw new Error('slug already exists')
  }
}

function requireOwned(id: string, authorId: string): Post {
  const post = store.find((p) => p.id === id)
  if (!post) {
    throw new Error('not found')
  }
  if (post.authorId !== authorId) {
    throw new Error('forbidden')
  }
  return post
}

function applyWrite(post: Post, payload: PostWritePayload) {
  post.title = payload.title.trim()
  post.slug = payload.slug.trim().toLowerCase()
  post.summary = (payload.summary ?? '').trim()
  post.contentMd = payload.contentMd ?? ''
  post.coverUrl = payload.coverUrl?.trim() || undefined
  post.tags = [...(payload.tags ?? [])]
}

export const mockPostRepository: PostRepository = {
  async list(params = {}) {
    return paginate(filterPosts(publishedPosts(), params), params.page ?? 1, params.pageSize ?? 10)
  },

  async getBySlug(slug) {
    return publishedPosts().find((p) => p.slug === slug) ?? null
  },

  async getLatest(count) {
    return publishedPosts().slice(0, count)
  },

  async listMine(params: MyPostListParams = {}) {
    const authorId = requireUserId()
    let items = store
      .filter((p) => p.authorId === authorId)
      .sort((a, b) => new Date(b.publishedAt || 0).getTime() - new Date(a.publishedAt || 0).getTime())
    if (params.status) {
      items = items.filter((p) => p.status === params.status)
    }
    return paginate(items, params.page ?? 1, params.pageSize ?? 10)
  },

  async getMineById(id) {
    return { ...requireOwned(id, requireUserId()) }
  },

  async create(payload) {
    const author = requireUser()
    const authorId = String(author.id)
    assertSlugAvailable(payload.slug.trim().toLowerCase())
    seq += 1
    const post: Post = {
      id: String(seq),
      title: payload.title.trim(),
      slug: payload.slug.trim().toLowerCase(),
      summary: (payload.summary ?? '').trim(),
      contentMd: payload.contentMd ?? '',
      coverUrl: payload.coverUrl?.trim() || undefined,
      tags: [...(payload.tags ?? [])],
      publishedAt: '',
      status: 'draft',
      authorId,
      authorDisplayName: author.displayName,
    }
    store.unshift(post)
    return { ...post }
  },

  async update(id, payload) {
    const post = requireOwned(id, requireUserId())
    assertSlugAvailable(payload.slug.trim().toLowerCase(), id)
    applyWrite(post, payload)
    return { ...post }
  },

  async publish(id) {
    const post = requireOwned(id, requireUserId())
    if (!post.title || !post.slug || !post.contentMd) {
      throw new Error('title, slug and content are required to publish')
    }
    post.status = 'published' satisfies PublishStatus
    if (!post.publishedAt) {
      post.publishedAt = new Date().toISOString()
    }
    return { ...post }
  },

  async unpublish(id) {
    const post = requireOwned(id, requireUserId())
    post.status = 'draft'
    return { ...post }
  },

  async remove(id) {
    const authorId = requireUserId()
    requireOwned(id, authorId)
    const idx = store.findIndex((p) => p.id === id)
    if (idx >= 0) store.splice(idx, 1)
  },

  async uploadImage(file) {
    requireUserId()
    return URL.createObjectURL(file)
  },
}
