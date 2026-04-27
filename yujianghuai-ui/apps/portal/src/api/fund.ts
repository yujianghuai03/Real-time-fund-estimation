import { request } from '@yujianghuai/shared'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface FundSearchItem {
  code: string
  name: string
  type?: string
  company?: string
  nav?: number
  navDate?: string
}

export interface FundEstimateRow {
  id: number
  code: string
  name: string
  holdingAmount: number
  navDate?: string
  previousNav?: number
  estimateNav?: number
  estimateRate?: number
  estimateProfit?: number
  estimateMarketValue?: number
  estimateTime?: string
  error?: string
}

export async function searchFunds(keyword: string) {
  const response = await request.get<ApiResult<FundSearchItem[]>>('/api/funds/search', {
    params: { keyword }
  })
  return response.data.data
}

export async function listWatchFunds(signal?: AbortSignal) {
  const response = await request.get<ApiResult<FundEstimateRow[]>>('/api/funds/watchlist', { signal })
  return response.data.data
}

export async function addWatchFund(code: string, name: string, holdingAmount: number) {
  const response = await request.post<ApiResult<FundEstimateRow>>('/api/funds/watchlist', {
    code,
    name,
    holdingAmount
  })
  return response.data.data
}

export async function updateFundHolding(code: string, holdingAmount: number) {
  await request.put(`/api/funds/watchlist/${code}/holding`, { holdingAmount })
}

export async function deleteWatchFund(code: string) {
  await request.delete(`/api/funds/watchlist/${code}`)
}
