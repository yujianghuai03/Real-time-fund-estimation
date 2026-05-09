import { request } from '@/api/http'
import type { OAuthTokenResponse } from '@/utils/authStorage'

const OAUTH_CLIENT_ID = import.meta.env.VITE_OAUTH_CLIENT_ID || 'yujianghuai-client'
const OAUTH_CLIENT_SECRET = import.meta.env.VITE_OAUTH_CLIENT_SECRET || 'yujianghuai-secret'
const OAUTH_SCOPE = import.meta.env.VITE_OAUTH_SCOPE || 'email'

interface EmailCodeLoginParams {
  tenantId: string
  email: string
  code: string
}

interface EmailCodeRegisterParams {
  tenantId: string
  username: string
  email: string
  password: string
  code: string
}

export interface RegisterResponse extends Partial<OAuthTokenResponse> {
  username: string
  nickname?: string
  tenantName?: string
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
  return request<boolean>('/admin-api/email/verification-code', {
    method: 'POST',
    tenantId,
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email })
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

export const registerByEmailCode = ({
  tenantId,
  username,
  email,
  password,
  code
}: EmailCodeRegisterParams): Promise<RegisterResponse> => {
  return request<RegisterResponse>('/auth/register', {
    method: 'POST',
    tenantId,
    headers: {
      'Content-Type': 'application/json',
      'LOGIN-TYPE': 'PORTAL'
    },
    body: JSON.stringify({
      tenantId: Number(tenantId),
      username,
      nickname: username,
      email,
      password,
      code
    })
  })
}
