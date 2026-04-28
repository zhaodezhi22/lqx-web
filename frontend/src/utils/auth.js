import { ElMessage } from 'element-plus'
import router from '../router'
import { closeChatSocket } from './chatSocketManager'

const AUTH_EXPIRED_EVENT = 'lqx-auth-expired'
let expiryTimer = null
let handlingAuthExpired = false
let messageShown = false

const clearExpiryTimer = () => {
  if (expiryTimer) {
    window.clearTimeout(expiryTimer)
    expiryTimer = null
  }
}

const decodeJwtPayload = (token) => {
  if (!token) return null
  const parts = token.split('.')
  if (parts.length < 2) return null
  try {
    const normalized = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    return JSON.parse(window.atob(padded))
  } catch (e) {
    return null
  }
}

export const getTokenExpireAt = (token) => {
  const payload = decodeJwtPayload(token)
  if (!payload?.exp) return null
  return payload.exp * 1000
}

export const clearAuthStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('avatar')
}

export const startTokenExpiryMonitor = () => {
  clearExpiryTimer()
  const token = localStorage.getItem('token')
  if (!token) return
  const expireAt = getTokenExpireAt(token)
  if (!expireAt) return

  const delay = expireAt - Date.now()
  if (delay <= 0) {
    handleAuthExpired('登录状态已过期，请重新登录')
    return
  }

  expiryTimer = window.setTimeout(() => {
    handleAuthExpired('登录状态已过期，请重新登录')
  }, delay)
}

export const handleAuthExpired = (message = '登录状态已过期，请重新登录') => {
  clearExpiryTimer()
  if (handlingAuthExpired) return
  handlingAuthExpired = true

  closeChatSocket()
  window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT))
  clearAuthStorage()

  if (message && !messageShown) {
    ElMessage.warning(message)
    messageShown = true
  }

  const currentRoute = router.currentRoute.value
  const isLoginPage = currentRoute?.name === 'Login'
  if (!isLoginPage) {
    const redirect = currentRoute?.fullPath && currentRoute.fullPath !== '/login'
      ? currentRoute.fullPath
      : '/'
    router.replace({
      name: 'Login',
      query: { redirect }
    })
  }

  window.setTimeout(() => {
    handlingAuthExpired = false
    messageShown = false
  }, 300)
}

export const getAuthExpiredEventName = () => AUTH_EXPIRED_EVENT
