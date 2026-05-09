import {
  clearAuthSession,
  getStoredAccessToken,
  getStoredAuthorization,
  getStoredTenantId,
  isTokenExpired,
} from '@/utils/authStorage'

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status?: number
  ) {
    super(message)
  }
}

interface RequestOptions extends RequestInit {
  tenantId?: string
}

interface R<T> {
  code: number
  message: string
  data: T
}

const isRecord = (value: unknown): value is Record<string, unknown> => {
  return typeof value === 'object' && value !== null
}

const isWrappedResponse = <T>(value: unknown): value is R<T> => {
  return isRecord(value) && typeof value.code === 'number' && 'data' in value
}

const parseResponse = async (response: Response): Promise<unknown> => {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

const PUBLIC_AUTH_PATHS = ['/auth2', '/oauth2']
const PUBLIC_AUTH_EXACT_PATHS = ['/admin-api/email/verification-code']
const TENANT_HEADER_NAME = 'TENANT-ID'

const resolveRequestPath = (url: string): string => {
  try {
    return new URL(url, window.location.origin).pathname
  } catch {
    return url.split('?')[0] || url
  }
}

const isPublicAuthRequest = (url: string): boolean => {
  const path = resolveRequestPath(url)

  return (
    PUBLIC_AUTH_EXACT_PATHS.includes(path) ||
    PUBLIC_AUTH_PATHS.some((publicPath) => path === publicPath || path.startsWith(`${publicPath}/`))
  )
}

const redirectToLogin = (): void => {
  clearAuthSession()
  window.dispatchEvent(new CustomEvent('auth:login-required'))
}

const resolveErrorMessage = (payload: unknown, fallback: string): string => {
  if (isRecord(payload)) {
    const message = payload.message || payload.error_description || payload.error
    return typeof message === 'string' ? message : fallback
  }

  return fallback
}

const resolveTenantId = (options: RequestOptions): string => {
  const hasRequestTenantId = Object.prototype.hasOwnProperty.call(options, 'tenantId')
  const tenantId = (hasRequestTenantId ? options.tenantId : getStoredTenantId())?.trim()

  if (!tenantId) {
    throw new ApiError('租户 ID 不能为空，请先选择或输入租户 ID', 400)
  }

  return tenantId
}

export const request = async <T>(url: string, options: RequestOptions = {}): Promise<T> => {
  const headers = new Headers(options.headers)
  const tenantId = resolveTenantId(options)
  const isPublicRequest = isPublicAuthRequest(url)

  headers.delete('Authorization')
  headers.set(TENANT_HEADER_NAME, tenantId)

  if (!isPublicRequest) {
    const accessToken = getStoredAccessToken()

    if (!accessToken || isTokenExpired(accessToken)) {
      redirectToLogin()
      throw new ApiError('登录已过期，请重新登录', 401)
    }

    const authorization = getStoredAuthorization()

    if (!authorization) {
      redirectToLogin()
      throw new ApiError('登录已过期，请重新登录', 401)
    }

    headers.set('Authorization', authorization)
  }

  const response = await fetch(url, {
    ...options,
    headers
  })
  const payload = await parseResponse(response)

  if (!response.ok) {
    throw new ApiError(resolveErrorMessage(payload, '请求失败'), response.status)
  }

  if (isWrappedResponse<T>(payload)) {
    if (payload.code !== 200) {
      throw new ApiError(payload.message || '请求失败', payload.code)
    }

    return payload.data
  }

  return payload as T
}
