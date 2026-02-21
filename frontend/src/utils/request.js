import axios from 'axios'
import router from '../router'

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
  (response) => response.data,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      router.push({ name: 'Login' })
    }
    return Promise.reject(error)
  }
)

export default service

