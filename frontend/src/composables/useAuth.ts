import { computed, ref } from 'vue'
import type { AuthUserInfo } from '@/types/auth'
import { authRepo } from '@/services'
import { getStoredToken, isAuthApiEnabled, setStoredToken } from '@/utils/http'

const user = ref<AuthUserInfo | null>(null)
const bootstrapped = ref(false)
const loading = ref(false)

export function useAuth() {
  const isLoggedIn = computed(() => !!user.value)
  const authEnabled = computed(() => isAuthApiEnabled())

  async function bootstrap() {
    if (bootstrapped.value) return
    bootstrapped.value = true
    const token = getStoredToken()
    if (!token) return
    try {
      user.value = await authRepo.me()
    } catch {
      setStoredToken(null)
      user.value = null
    }
  }

  async function login(username: string, password: string) {
    loading.value = true
    try {
      const result = await authRepo.login({ username, password })
      setStoredToken(result.accessToken)
      user.value = result.user
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
      user.value = result.user
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
      user.value = result.user
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
      user.value = null
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
