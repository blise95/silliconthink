import type { PostRepository } from '@/services/postRepository'
import type { MyPostListParams, Paginated, Post, PostListParams, PostWritePayload } from '@/types'
import { ApiError, apiRequest, getApiBaseUrl, getStoredToken } from '@/utils/http'
import type { ApiResult } from '@/types/auth'

function qs(params: Record<string, string | number | undefined>): string {
  const sp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== '') sp.set(k, String(v))
  })
  const s = sp.toString()
  return s ? `?${s}` : ''
}

async function uploadImageRequest(file: File): Promise<string> {
  const headers = new Headers()
  const token = getStoredToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const body = new FormData()
  body.append('file', file)
  const res = await fetch(`${getApiBaseUrl()}/api/v1/me/media/images`, {
    method: 'POST',
    headers,
    body,
  })
  const payload = (await res.json()) as ApiResult<{ url: string }>
  if (!res.ok || payload.code !== 0) {
    throw new Error(payload.message || 'upload failed')
  }
  const url = payload.data.url
  if (url.startsWith('http')) return url
  return `${getApiBaseUrl()}${url}`
}

export const apiPostRepository: PostRepository = {
  list(params: PostListParams = {}) {
    return apiRequest<Paginated<Post>>(
      `/api/v1/posts${qs({
        page: params.page,
        pageSize: params.pageSize,
        tag: params.tag,
        keyword: params.keyword,
      })}`,
    )
  },

  async getBySlug(slug) {
    try {
      return await apiRequest<Post>(`/api/v1/posts/by-slug/${encodeURIComponent(slug)}`)
    } catch (e) {
      // 仅 404 视为不存在；其它错误向上抛，交给页面展示错误态
      if (e instanceof ApiError && (e.status === 404 || e.code === 40400)) {
        return null
      }
      throw e
    }
  },

  getLatest(count) {
    return apiRequest<Post[]>(`/api/v1/posts/latest${qs({ count })}`)
  },

  listMine(params: MyPostListParams = {}) {
    return apiRequest<Paginated<Post>>(
      `/api/v1/me/posts${qs({
        page: params.page,
        pageSize: params.pageSize,
        status: params.status || undefined,
      })}`,
    )
  },

  getMineById(id) {
    return apiRequest<Post>(`/api/v1/me/posts/${id}`)
  },

  create(payload: PostWritePayload) {
    return apiRequest<Post>('/api/v1/me/posts', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  update(id, payload) {
    return apiRequest<Post>(`/api/v1/me/posts/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  publish(id) {
    return apiRequest<Post>(`/api/v1/me/posts/${id}/publish`, { method: 'POST' })
  },

  unpublish(id) {
    return apiRequest<Post>(`/api/v1/me/posts/${id}/unpublish`, { method: 'POST' })
  },

  async remove(id) {
    await apiRequest<null>(`/api/v1/me/posts/${id}`, { method: 'DELETE' })
  },

  uploadImage(file) {
    return uploadImageRequest(file)
  },
}
