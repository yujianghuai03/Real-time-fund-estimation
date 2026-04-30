import { request } from '@yujianghuai/shared'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export type FundGroupId = string | number

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
  holdingCostNav?: number
  holdingShares?: number
  firstBuyDate?: string
  navDate?: string
  previousNav?: number
  estimateNav?: number
  estimateRate?: number
  estimateProfit?: number
  estimateMarketValue?: number
  estimateTime?: string
  error?: string
  groupIds?: FundGroupId[]
}

export interface MarketIndexRow {
  code: string
  name: string
  price?: number
  change?: number
  changeRate?: number
}

export interface FundGroup {
  id: FundGroupId
  name: string
  count: number
  groupType?: 'SYSTEM' | 'CUSTOM' | string
  editable?: boolean
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
    holdingCostNav?: number
    holdingShares?: number
    firstBuyDate?: string
    navDate?: string
    previousNav?: number
    estimateNav?: number
    estimateRate?: number
    estimateProfit?: number
    estimateMarketValue?: number
    estimateTime?: string
    error?: string
    groupIds: FundGroupId[]
  }>
  groups: Array<{
    id: FundGroupId
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

export async function listMarketIndices() {
  const response = await request.get<ApiResult<MarketIndexRow[]>>('/api/funds/indices')
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

export async function updateFundGroup(id: FundGroupId, name: string) {
  const response = await request.put<ApiResult<FundGroup>>(`/api/funds/groups/${id}`, { name })
  return response.data.data
}

export async function deleteFundGroup(id: FundGroupId) {
  await request.delete(`/api/funds/groups/${id}`)
}

export async function addWatchFund(code: string, name: string, holdingAmount: number, holdingCost = 0, holdingCostNav = 0, holdingShares = 0, firstBuyDate = '') {
  const response = await request.post<ApiResult<FundEstimateRow>>('/api/funds/watchlist', {
    code,
    name,
    holdingAmount,
    holdingCost,
    holdingCostNav,
    holdingShares,
    firstBuyDate: firstBuyDate || null
  })
  return response.data.data
}

export async function updateFundHolding(code: string, holdingAmount: number, holdingCost = 0, holdingCostNav = 0, holdingShares = 0, firstBuyDate = '') {
  await request.put(`/api/funds/watchlist/${code}/holding`, {
    holdingAmount,
    holdingCost,
    holdingCostNav,
    holdingShares,
    firstBuyDate: firstBuyDate || null
  })
}

export async function updateWatchFundGroups(code: string, groupIds: FundGroupId[]) {
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
