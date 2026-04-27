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
  holdingCost?: number
  navDate?: string
  previousNav?: number
  estimateNav?: number
  estimateRate?: number
  estimateProfit?: number
  estimateMarketValue?: number
  estimateTime?: string
  error?: string
  groupIds?: number[]
}

export interface FundGroup {
  id: number
  name: string
  count: number
}

export interface FundTransaction {
  id?: number
  fundCode: string
  fundName: string
  tradeType: string
  amount: number
  beforeAmount: number
  afterAmount: number
  targetFundCode?: string
  targetFundName?: string
  remark?: string
  tradeTime: string
}

export interface FundSnapshot {
  funds: Array<{
    code: string
    name: string
    holdingAmount: number
    holdingCost?: number
    navDate?: string
    previousNav?: number
    estimateNav?: number
    estimateRate?: number
    estimateProfit?: number
    estimateMarketValue?: number
    estimateTime?: string
    error?: string
    groupIds: number[]
  }>
  groups: Array<{
    id: number
    name: string
  }>
  transactions: FundTransaction[]
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

export async function estimateFund(code: string) {
  const response = await request.get<ApiResult<FundEstimateRow>>(`/api/funds/estimate/${code}`)
  return response.data.data
}

export async function listFundGroups() {
  const response = await request.get<ApiResult<FundGroup[]>>('/api/funds/groups')
  return response.data.data
}

export async function listFundTransactions() {
  const response = await request.get<ApiResult<FundTransaction[]>>('/api/funds/transactions')
  return response.data.data
}

export async function createFundGroup(name: string) {
  const response = await request.post<ApiResult<FundGroup>>('/api/funds/groups', { name })
  return response.data.data
}

export async function updateFundGroup(id: number, name: string) {
  const response = await request.put<ApiResult<FundGroup>>(`/api/funds/groups/${id}`, { name })
  return response.data.data
}

export async function deleteFundGroup(id: number) {
  await request.delete(`/api/funds/groups/${id}`)
}

export async function addWatchFund(code: string, name: string, holdingAmount: number, holdingCost = 0) {
  const response = await request.post<ApiResult<FundEstimateRow>>('/api/funds/watchlist', {
    code,
    name,
    holdingAmount,
    holdingCost
  })
  return response.data.data
}

export async function updateFundHolding(code: string, holdingAmount: number, holdingCost = 0) {
  await request.put(`/api/funds/watchlist/${code}/holding`, { holdingAmount, holdingCost })
}

export async function updateWatchFundGroups(code: string, groupIds: number[]) {
  await request.put(`/api/funds/watchlist/${code}/groups`, { groupIds })
}

export async function deleteWatchFund(code: string) {
  await request.delete(`/api/funds/watchlist/${code}`)
}

export async function replaceCloudSnapshot(snapshot: FundSnapshot) {
  await request.post('/api/funds/snapshot/replace', snapshot)
}

export async function mergeCloudSnapshot(snapshot: FundSnapshot) {
  await request.post('/api/funds/snapshot/merge', snapshot)
}
