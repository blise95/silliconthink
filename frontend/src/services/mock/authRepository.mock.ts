import type { AuthRepository } from '@/services/authRepository'
import type { AuthTokenPayload, AuthUserInfo, LoginPayload, RegisterPayload } from '@/types/auth'
import { getStoredToken } from '@/utils/http'
import { loadPersistedUserSnapshot, setUserSnapshot } from '@/services/mock/userSnapshot'

const DEMO_USER: AuthUserInfo = {
  id: 1,
  username: 'demo',
  displayName: 'Demo User',
}

let nextId = 2

function requireSession(): AuthUserInfo {
  if (!getStoredToken()) {
    throw new Error('unauthorized')
  }
  const user = loadPersistedUserSnapshot()
  if (!user) {
    throw new Error('unauthorized')
  }
  return user
}

export const mockAuthRepository: AuthRepository = {
  async login(payload: LoginPayload) {
    if (payload.username !== 'demo' || payload.password !== 'demo1234') {
      throw new Error('invalid username or password')
    }
    setUserSnapshot(DEMO_USER, true)
    return {
      accessToken: 'mock-token',
      tokenType: 'Bearer',
      user: DEMO_USER,
    } satisfies AuthTokenPayload
  },
  async register(payload: RegisterPayload) {
    const user: AuthUserInfo = {
      id: nextId++,
      username: payload.username,
      displayName: payload.displayName || payload.username,
    }
    setUserSnapshot(user, true)
    return {
      accessToken: 'mock-token',
      tokenType: 'Bearer',
      user,
    } satisfies AuthTokenPayload
  },
  async me() {
    return requireSession()
  },
  async logout() {
    setUserSnapshot(null, true)
  },
  async exchangeOAuthCode() {
    throw new Error('OAuth is not available in mock mode')
  },
  getGitHubAuthorizeUrl() {
    return '#'
  },
}
