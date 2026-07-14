import type { ApiResult } from '@/types/auth'

const TOKEN_KEY = 'st_access_token'

export function getApiBaseUrl(): string {
  // 显式配置（含空字符串）优先：空字符串 = 同源（Nginx 反代 /api）
  if (typeof import.meta.env.VITE_API_BASE_URL === 'string') {
    return import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')
  }
  return 'http://localhost:8080'
}

export function isAuthApiEnabled(): boolean {
  return import.meta.env.VITE_AUTH_USE_API === 'true'
}

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export class ApiError extends Error {
  code: number
  status: number

  constructor(message: string, code: number, status: number) {
    super(message)
    this.code = code
    this.status = status
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers || {})
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getStoredToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const res = await fetch(`${getApiBaseUrl()}${path}`, {
    ...options,
    headers,
  })

  let payload: ApiResult<T> | null = null
  try {
    payload = (await res.json()) as ApiResult<T>
  } catch {
    throw new ApiError('invalid response', -1, res.status)
  }

  if (!res.ok || payload.code !== 0) {
    throw new ApiError(payload.message || 'request failed', payload.code, res.status)
  }
  return payload.data
}
