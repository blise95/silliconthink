import type { AuthUserInfo } from '@/types/auth'

/** 供 Mock 内容层读取当前用户，避免依赖 useAuth 造成循环引用 */
const STORAGE_KEY = 'st_mock_user'

let snapshot: AuthUserInfo | null = null

export function setUserSnapshot(user: AuthUserInfo | null, persist = false) {
  snapshot = user
  if (user === null) {
    localStorage.removeItem(STORAGE_KEY)
    return
  }
  if (persist) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  }
}

export function getUserSnapshot(): AuthUserInfo | null {
  return snapshot
}

/** Mock 认证刷新页面时恢复会话（仅 mock auth 使用） */
export function loadPersistedUserSnapshot(): AuthUserInfo | null {
  if (snapshot) return snapshot
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    snapshot = JSON.parse(raw) as AuthUserInfo
    return snapshot
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}
