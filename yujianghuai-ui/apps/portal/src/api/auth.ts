import { request } from '@yujianghuai/shared'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface TenantOption {
  id: number
  tenantCode: string
  tenantName: string
}

export interface RegisterPayload {
  tenantId: number
  username: string
  nickname: string
  password: string
}

export interface RegisterResult {
  username: string
  nickname: string
  tenantName: string
}

export interface UserInfo {
  sub?: string
  username?: string
  tenant_id?: string
  tenant_name?: string
  authorities?: string[]
  client_id?: string
  avatar?: string
  [key: string]: unknown
}

export async function login(username: string, password: string, tenantId: string) {
  const params = new URLSearchParams()
  params.set('grant_type', 'password')
  params.set('username', username)
  params.set('password', password)
  params.set('scope', 'openid profile api.read api.write')
  params.set('TENANT-ID', tenantId)
  params.set('LOGIN-TYPE', 'PORTAL')
  const basic = btoa('yujianghuai-client:yujianghuai-secret')
  const response = await request.post('/oauth2/token', params, {
    headers: {
      Authorization: `Basic ${basic}`,
      'Content-Type': 'application/x-www-form-urlencoded',
      'TENANT-ID': tenantId,
      'LOGIN-TYPE': 'PORTAL'
    }
  })
  return response.data
}

export async function getTenants() {
  const response = await request.get<ApiResult<TenantOption[]>>('/auth/tenants')
  return response.data.data
}

export async function register(payload: RegisterPayload) {
  const response = await request.post<ApiResult<RegisterResult>>('/auth/register', payload)
  return response.data.data
}

export async function getUserInfo() {
  const response = await request.get<ApiResult<UserInfo>>('/token/userinfo')
  return response.data.data
}

export async function logout() {
  const response = await request.delete<ApiResult<boolean>>('/token/logout')
  return response.data.data
}
