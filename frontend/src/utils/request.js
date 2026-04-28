import axios from 'axios'
import { handleAuthExpired } from './auth'

const service = axios.create({
  baseURL: '/api', // 可按需调整
  timeout: 15000,
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    if (response?.data?.code === 401 && localStorage.getItem('token')) {
      handleAuthExpired(response.data.message || '登录状态已过期，请重新登录')
    }
    return response.data
  },
  (error) => {
    const status = error?.response?.status
    const code = error?.response?.data?.code
    if ((status === 401 || code === 401) && localStorage.getItem('token')) {
      handleAuthExpired(error?.response?.data?.message || '登录状态已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

export default service

