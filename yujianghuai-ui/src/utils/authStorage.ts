export interface OAuthTokenResponse {
  access_token: string
  refresh_token?: string
  token_type?: string
  expires_in?: number
  scope?: string
}

export interface AuthSession {
  accessToken: string
  tokenType: string
  tenantId: string
  email: string
  expiresAt: number
}

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const TOKEN_TYPE_KEY = 'tokenType'
const TENANT_ID_KEY = 'tenantId'
const USER_EMAIL_KEY = 'userEmail'
const EXPIRES_AT_KEY = 'expiresAt'
const DEFAULT_TENANT_ID = '1'
const DEFAULT_TOKEN_TYPE = 'Bearer'

interface JwtPayload {
  exp?: number
  email?: string
  sub?: string
  tenant_id?: string | number
}

const decodeBase64Url = (value: string): string => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=')
  return decodeURIComponent(
    atob(padded)
      .split('')
      .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
      .join('')
  )
}

const parseJwtPayload = (token: string): JwtPayload => {
  const [, payload] = token.split('.')

  if (!payload) {
    return {}
  }

  try {
    return JSON.parse(decodeBase64Url(payload)) as JwtPayload
  } catch {
    return {}
  }
}

export const getStoredTenantId = (): string => {
  return window.localStorage.getItem(TENANT_ID_KEY) || DEFAULT_TENANT_ID
}

export const getStoredAccessToken = (): string => {
  const token = window.localStorage.getItem(ACCESS_TOKEN_KEY)?.trim()

  if (!token || token === 'null' || token === 'undefined') {
    return ''
  }

  return token
}

export const isTokenExpired = (token: string): boolean => {
  if (!token || token === 'null' || token === 'undefined') {
    return true
  }

  const payload = parseJwtPayload(token)

  if (!payload.exp) {
    return true
  }

  return payload.exp * 1000 <= Date.now()
}

export const getStoredAuthorization = (): string => {
  const token = getStoredAccessToken()

  if (!token || isTokenExpired(token)) {
    return ''
  }

  return `${window.localStorage.getItem(TOKEN_TYPE_KEY) || DEFAULT_TOKEN_TYPE} ${token}`
}

export const getStoredEmail = (): string => {
  return window.localStorage.getItem(USER_EMAIL_KEY) || ''
}

export const saveTenantId = (tenantId: string): void => {
  window.localStorage.setItem(TENANT_ID_KEY, tenantId || DEFAULT_TENANT_ID)
}

export const saveOAuthToken = (
  tokenResponse: OAuthTokenResponse,
  tenantId: string,
  fallbackEmail: string
): AuthSession => {
  const payload = parseJwtPayload(tokenResponse.access_token)
  const resolvedTenantId = String(payload.tenant_id || tenantId || DEFAULT_TENANT_ID)
  const email = payload.email || payload.sub || fallbackEmail
  const tokenType = tokenResponse.token_type || DEFAULT_TOKEN_TYPE
  const expiresAt = payload.exp ? payload.exp * 1000 : Date.now() + (tokenResponse.expires_in || 7200) * 1000

  window.localStorage.setItem(ACCESS_TOKEN_KEY, tokenResponse.access_token)
  if (tokenResponse.refresh_token) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, tokenResponse.refresh_token)
  }
  window.localStorage.setItem(TOKEN_TYPE_KEY, tokenType)
  window.localStorage.setItem(TENANT_ID_KEY, resolvedTenantId)
  window.localStorage.setItem(USER_EMAIL_KEY, email)
  window.localStorage.setItem(EXPIRES_AT_KEY, String(expiresAt))

  return {
    accessToken: tokenResponse.access_token,
    tokenType,
    tenantId: resolvedTenantId,
    email,
    expiresAt
  }
}

export const getStoredAuthSession = (): AuthSession | null => {
  const accessToken = getStoredAccessToken()

  if (!accessToken) {
    return null
  }

  const expiresAt = Number(window.localStorage.getItem(EXPIRES_AT_KEY) || 0)
  if (isTokenExpired(accessToken) || (expiresAt && expiresAt <= Date.now())) {
    return null
  }

  return {
    accessToken,
    tokenType: window.localStorage.getItem(TOKEN_TYPE_KEY) || DEFAULT_TOKEN_TYPE,
    tenantId: getStoredTenantId(),
    email: getStoredEmail(),
    expiresAt
  }
}

export const clearAuthSession = (): void => {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
  window.localStorage.removeItem(REFRESH_TOKEN_KEY)
  window.localStorage.removeItem(TOKEN_TYPE_KEY)
  window.localStorage.removeItem(USER_EMAIL_KEY)
  window.localStorage.removeItem(EXPIRES_AT_KEY)
}
