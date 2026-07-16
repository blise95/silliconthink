import { computed, ref } from 'vue'
import type { AuthUserInfo } from '@/types/auth'
import { authRepo } from '@/services'
import { setUserSnapshot } from '@/services/mock/userSnapshot'
import { getStoredToken, isAuthApiEnabled, setStoredToken } from '@/utils/http'

const user = ref<AuthUserInfo | null>(null)
const bootstrapped = ref(false)
const loading = ref(false)
/** 并发 bootstrap 共用同一 Promise，避免守卫在 me() 未完成时误判未登录 */
let bootstrapPromise: Promise<void> | null = null

function applyUser(next: AuthUserInfo | null) {
  user.value = next
  // 同步给 Mock 内容层（支持「真实认证 + Mock 文章」）
  setUserSnapshot(next)
}

export function useAuth() {
  const isLoggedIn = computed(() => !!user.value)
  const authEnabled = computed(() => isAuthApiEnabled())

  async function bootstrap() {
    if (bootstrapped.value) return
    if (bootstrapPromise) return bootstrapPromise

    bootstrapPromise = (async () => {
      const token = getStoredToken()
      if (!token) return
      try {
        applyUser(await authRepo.me())
      } catch {
        setStoredToken(null)
        applyUser(null)
      }
    })().finally(() => {
      bootstrapped.value = true
      bootstrapPromise = null
    })

    return bootstrapPromise
  }

  async function login(username: string, password: string) {
    loading.value = true
    try {
      const result = await authRepo.login({ username, password })
      setStoredToken(result.accessToken)
      applyUser(result.user)
      bootstrapped.value = true
      return result.user
    } finally {
      loading.value = false
    }
  }

  async function register(username: string, password: string, displayName?: string) {
    loading.value = true
    try {
      const result = await authRepo.register({ username, password, displayName })
      setStoredToken(result.accessToken)
      applyUser(result.user)
      bootstrapped.value = true
      return result.user
    } finally {
      loading.value = false
    }
  }

  async function exchangeOAuthCode(code: string) {
    loading.value = true
    try {
      const result = await authRepo.exchangeOAuthCode(code)
      setStoredToken(result.accessToken)
      applyUser(result.user)
      bootstrapped.value = true
      return result.user
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      if (getStoredToken()) {
        await authRepo.logout()
      }
    } catch {
      // ignore network errors on logout
    } finally {
      setStoredToken(null)
      applyUser(null)
      bootstrapped.value = true
    }
  }

  function startGitHubOAuth() {
    window.location.href = authRepo.getGitHubAuthorizeUrl()
  }

  return {
    user,
    loading,
    isLoggedIn,
    authEnabled,
    bootstrap,
    login,
    register,
    exchangeOAuthCode,
    logout,
    startGitHubOAuth,
  }
}
