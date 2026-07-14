/** API 统一响应 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface AuthUserInfo {
  id: number
  username: string
  displayName: string
}

export interface AuthTokenPayload {
  accessToken: string
  tokenType: string
  user: AuthUserInfo
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  displayName?: string
}
