import { request } from '@/api/http'
import type { OAuthTokenResponse } from '@/utils/authStorage'

const OAUTH_CLIENT_ID = import.meta.env.VITE_OAUTH_CLIENT_ID || 'yujianghuai-client'
const OAUTH_CLIENT_SECRET = import.meta.env.VITE_OAUTH_CLIENT_SECRET || 'yujianghuai-secret'
const OAUTH_SCOPE = import.meta.env.VITE_OAUTH_SCOPE || 'openid profile api.read api.write'

interface EmailCodeLoginParams {
  tenantId: string
  email: string
  code: string
}

interface PasswordLoginParams {
  tenantId: string
  username: string
  password: string
}

const buildTokenBody = (params: Record<string, string>) => {
  const body = new URLSearchParams({
    client_id: OAUTH_CLIENT_ID,
    client_secret: OAUTH_CLIENT_SECRET,
    scope: OAUTH_SCOPE,
    'TENANT-ID': params.tenantId,
    'LOGIN-TYPE': 'PORTAL',
    ...params
  })
  body.delete('tenantId')
  return body
}

export const sendEmailVerificationCode = (email: string, tenantId: string): Promise<boolean> => {
  const params = new URLSearchParams({ email })

  return request<boolean>(`/admin-api/email/verification-code?${params.toString()}`, {
    method: 'GET',
    tenantId
  })
}

export const loginByEmailCode = ({ tenantId, email, code }: EmailCodeLoginParams): Promise<OAuthTokenResponse> => {
  return request<OAuthTokenResponse>('/oauth2/token', {
    method: 'POST',
    tenantId,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'LOGIN-TYPE': 'PORTAL'
    },
    body: buildTokenBody({
      tenantId,
      grant_type: 'email_code',
      email,
      code
    })
  })
}

export const loginByPassword = ({ tenantId, username, password }: PasswordLoginParams): Promise<OAuthTokenResponse> => {
  return request<OAuthTokenResponse>('/oauth2/token', {
    method: 'POST',
    tenantId,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'LOGIN-TYPE': 'PORTAL'
    },
    body: buildTokenBody({
      tenantId,
      grant_type: 'password',
      username,
      password
    })
  })
}
