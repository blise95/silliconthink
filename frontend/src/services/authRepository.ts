import type { AuthTokenPayload, AuthUserInfo, LoginPayload, RegisterPayload } from '@/types/auth'

export interface AuthRepository {
  login(payload: LoginPayload): Promise<AuthTokenPayload>
  register(payload: RegisterPayload): Promise<AuthTokenPayload>
  me(): Promise<AuthUserInfo>
  logout(): Promise<void>
  exchangeOAuthCode(code: string): Promise<AuthTokenPayload>
  getGitHubAuthorizeUrl(): string
}
