import axios from 'axios'

const viteEnv = (import.meta as unknown as { env?: { VITE_API_BASE_URL?: string } }).env

export const request = axios.create({
  baseURL: viteEnv?.VITE_API_BASE_URL ?? '',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? window.localStorage.getItem('YJH_TOKEN') : ''
  const tenantId = typeof window !== 'undefined' ? window.localStorage.getItem('YJH_TENANT_ID') : ''
  config.headers = config.headers ?? {}
  const headers = config.headers as Record<string, string | undefined>
  if (token && !headers.Authorization && !headers.authorization) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (tenantId) {
    config.headers['TENANT-ID'] = tenantId
  }
  return config
})
