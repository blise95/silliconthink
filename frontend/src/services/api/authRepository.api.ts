import type { AuthRepository } from '@/services/authRepository'
import type { AuthTokenPayload, AuthUserInfo, LoginPayload, RegisterPayload } from '@/types/auth'
import { apiRequest, getApiBaseUrl } from '@/utils/http'

export const apiAuthRepository: AuthRepository = {
  login(payload: LoginPayload) {
    return apiRequest<AuthTokenPayload>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  register(payload: RegisterPayload) {
    return apiRequest<AuthTokenPayload>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  me() {
    return apiRequest<AuthUserInfo>('/api/v1/auth/me')
  },
  async logout() {
    await apiRequest<null>('/api/v1/auth/logout', { method: 'POST' })
  },
  exchangeOAuthCode(code: string) {
    return apiRequest<AuthTokenPayload>('/api/v1/auth/oauth/exchange', {
      method: 'POST',
      body: JSON.stringify({ code }),
    })
  },
  getGitHubAuthorizeUrl() {
    return `${getApiBaseUrl()}/api/v1/auth/oauth/github/authorize`
  },
}
