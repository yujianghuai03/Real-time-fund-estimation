import { getStoredAuthorization, getStoredTenantId } from '@/utils/authStorage'

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

const resolveErrorMessage = (payload: unknown, fallback: string): string => {
  if (isRecord(payload)) {
    const message = payload.message || payload.error_description || payload.error
    return typeof message === 'string' ? message : fallback
  }

  return fallback
}

export const request = async <T>(url: string, options: RequestOptions = {}): Promise<T> => {
  const headers = new Headers(options.headers)
  const authorization = getStoredAuthorization()
  const tenantId = options.tenantId || getStoredTenantId()

  if (authorization) {
    headers.set('Authorization', authorization)
  }

  if (tenantId) {
    headers.set('TENANT-ID', tenantId)
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
