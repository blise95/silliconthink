import type { AuthRepository } from '@/services/authRepository'
import type { AuthTokenPayload, AuthUserInfo, LoginPayload, RegisterPayload } from '@/types/auth'

const MOCK_USER: AuthUserInfo = {
  id: 1,
  username: 'demo',
  displayName: 'Demo User',
}

export const mockAuthRepository: AuthRepository = {
  async login(payload: LoginPayload) {
    if (payload.username !== 'demo' || payload.password !== 'demo1234') {
      throw new Error('invalid username or password')
    }
    return {
      accessToken: 'mock-token',
      tokenType: 'Bearer',
      user: MOCK_USER,
    } satisfies AuthTokenPayload
  },
  async register(payload: RegisterPayload) {
    return {
      accessToken: 'mock-token',
      tokenType: 'Bearer',
      user: {
        id: 2,
        username: payload.username,
        displayName: payload.displayName || payload.username,
      },
    } satisfies AuthTokenPayload
  },
  async me() {
    return MOCK_USER
  },
  async logout() {},
  async exchangeOAuthCode() {
    throw new Error('OAuth is not available in mock mode')
  },
  getGitHubAuthorizeUrl() {
    return '#'
  },
}
