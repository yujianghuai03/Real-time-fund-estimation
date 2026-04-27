import axios from 'axios'

const viteEnv = (import.meta as unknown as { env?: { VITE_API_BASE_URL?: string } }).env

export const request = axios.create({
  baseURL: viteEnv?.VITE_API_BASE_URL ?? '',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const skipAuth = config.url?.startsWith('/api/funds/search')
  const token = typeof window !== 'undefined' ? window.localStorage.getItem('YJH_TOKEN') : ''
  const tenantId = typeof window !== 'undefined' ? window.localStorage.getItem('YJH_TENANT_ID') : ''
  config.headers = config.headers ?? {}
  const headers = config.headers as Record<string, string | undefined>
  delete headers['X-Skip-Auth']
  if (!skipAuth && token && !headers.Authorization && !headers.authorization) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (tenantId) {
    config.headers['TENANT-ID'] = tenantId
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      error.message = '权限不足，请登录后再试！'
      if (error.response.data && typeof error.response.data === 'object') {
        error.response.data.message = '权限不足，请登录后再试！'
        error.response.data.error_description = '权限不足，请登录后再试！'
      }
    }
    return Promise.reject(error)
  }
)
