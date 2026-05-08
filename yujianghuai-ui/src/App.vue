<template>
  <div class="fund-platform theme-transition">
    <AppHeader />

    <div class="fund-platform__body">
      <AppAside
        :active-key="activeMenuKey"
        :menu-items="asideMenuItems"
        :is-loading="isMenuLoading"
        :load-error="menuLoadError"
        :status-text="menuStatusText"
        @select="handleMenuSelect"
      />

      <main class="fund-platform__main">
        <section class="info-hero">
          <div class="info-hero__copy">
            <p class="info-hero__eyebrow">{{ activeContent.eyebrow }}</p>
            <h1>{{ activeContent.title }}</h1>
            <p>{{ activeContent.description }}</p>
          </div>
          <div class="info-hero__actions">
            <el-input v-model="filterKeyword" :placeholder="activeContent.searchPlaceholder" clearable />
            <el-button type="primary" @click="refreshView">{{ activeContent.primaryAction }}</el-button>
          </div>
        </section>

        <section class="metric-grid" :aria-label="`${activeContent.title}指标`">
          <article v-for="metric in activeContent.metrics" :key="metric.label" class="metric-card">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <small :class="{ 'metric-card__trend--down': metric.trend.startsWith('-') }">{{ metric.trend }}</small>
          </article>
        </section>

        <section class="data-panel">
          <div class="data-panel__header">
            <div>
              <h2>{{ activeContent.panelTitle }}</h2>
              <p>{{ activeContent.panelDescription }}</p>
            </div>
            <el-button>{{ activeContent.secondaryAction }}</el-button>
          </div>

          <el-table
            v-if="activeContent.viewType === 'estimate'"
            :data="filteredEstimateRows"
            class="theme-table"
            height="300"
          >
            <el-table-column prop="code" label="基金代码" width="120" />
            <el-table-column prop="name" label="基金名称" min-width="160" />
            <el-table-column prop="estimate" label="估值" width="120" />
            <el-table-column prop="rate" label="涨跌幅" width="120">
              <template #default="{ row }">
                <span :class="['rate-text', row.rate.startsWith('-') ? 'rate-text--down' : 'rate-text--up']">
                  {{ row.rate }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="更新时间" width="160" />
          </el-table>

          <div v-else-if="activeContent.viewType === 'holding'" class="holding-list">
            <article v-for="holding in holdingRows" :key="holding.name" class="holding-row">
              <div>
                <strong>{{ holding.name }}</strong>
                <span>{{ holding.code }} · {{ holding.risk }}</span>
              </div>
              <div>
                <strong>{{ holding.amount }}</strong>
                <span :class="{ 'rate-text--down': holding.profit.startsWith('-'), 'rate-text--up': !holding.profit.startsWith('-') }">
                  {{ holding.profit }}
                </span>
              </div>
            </article>
          </div>

          <div v-else-if="activeContent.viewType === 'record'" class="timeline-list">
            <article v-for="record in tradeRecords" :key="record.id" class="timeline-row">
              <span>{{ record.time }}</span>
              <strong>{{ record.action }}</strong>
              <small>{{ record.detail }}</small>
            </article>
          </div>

          <div v-else class="group-grid">
            <article v-for="group in groupRows" :key="group.name" class="group-card">
              <strong>{{ group.name }}</strong>
              <span>{{ group.count }} 只基金</span>
              <small>{{ group.description }}</small>
            </article>
          </div>
        </section>
      </main>
    </div>

    <AppFooter :active-title="activeContent.title" :status-text="footerStatusText" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { fetchPortalMenus, type MenuTreeNode } from '@/api/menu'
import AppAside, { type AsideMenuItem } from '@/components/layout/fund/AppAside.vue'
import AppFooter from '@/components/layout/fund/AppFooter.vue'
import AppHeader from '@/components/layout/fund/AppHeader.vue'

interface MetricItem {
  label: string
  value: string
  trend: string
}

interface FundEstimateRow {
  code: string
  name: string
  estimate: string
  rate: string
  time: string
}

interface MainContent {
  key: string
  title: string
  eyebrow: string
  description: string
  searchPlaceholder: string
  primaryAction: string
  secondaryAction: string
  panelTitle: string
  panelDescription: string
  viewType: 'estimate' | 'holding' | 'record' | 'group'
  metrics: MetricItem[]
}

const fallbackMenus: AsideMenuItem[] = [
  { key: 'estimate', title: '实时估值', description: '盘中净值与涨跌', shortLabel: '估', badge: '12' },
  { key: 'holding', title: '持仓分析', description: '资产、收益、风险', shortLabel: '仓' },
  { key: 'record', title: '交易记录', description: '申购赎回流水', shortLabel: '记' },
  { key: 'group', title: '分组管理', description: '自选与关注池', shortLabel: '组' }
]

const contentMap: Record<string, MainContent> = {
  estimate: {
    key: 'estimate',
    title: '实时估值',
    eyebrow: 'Realtime Fund Estimation',
    description: '集中查看关注基金的估值、涨跌幅和更新时间，优先服务盘中快速判断。',
    searchPlaceholder: '筛选基金代码或名称',
    primaryAction: '刷新估值',
    secondaryAction: '导出数据',
    panelTitle: '实时估值列表',
    panelDescription: '涨跌状态配合符号与颜色展示，避免只依赖红绿判断。',
    viewType: 'estimate',
    metrics: [
      { label: '总资产估值', value: '256,840.1290', trend: '+2.3400%' },
      { label: '今日收益', value: '1,248.3700', trend: '+0.8600%' },
      { label: '关注基金', value: '36', trend: '12 个更新中' }
    ]
  },
  holding: {
    key: 'holding',
    title: '持仓分析',
    eyebrow: 'Position Insight',
    description: '把当前持仓、浮动收益和风险标签放在同一个视野内，方便复盘仓位结构。',
    searchPlaceholder: '筛选持仓基金',
    primaryAction: '同步持仓',
    secondaryAction: '查看明细',
    panelTitle: '持仓概览',
    panelDescription: '展示持仓金额、浮动收益和风险分层，后续可接入真实持仓接口。',
    viewType: 'holding',
    metrics: [
      { label: '持仓金额', value: '188,420.5500', trend: '+1.1200%' },
      { label: '浮动收益', value: '7,826.3300', trend: '+4.3400%' },
      { label: '高波动占比', value: '28%', trend: '较昨日 -2%' }
    ]
  },
  record: {
    key: 'record',
    title: '交易记录',
    eyebrow: 'Trade Activity',
    description: '按时间线查看申购、赎回和定投记录，让账户动作和估值变化能对上。',
    searchPlaceholder: '筛选交易基金',
    primaryAction: '同步记录',
    secondaryAction: '导出流水',
    panelTitle: '近期交易',
    panelDescription: '当前为页面结构占位，后续可接入交易流水分页接口。',
    viewType: 'record',
    metrics: [
      { label: '本月交易', value: '18', trend: '+4 笔' },
      { label: '定投计划', value: '6', trend: '2 个待执行' },
      { label: '待确认', value: '3', trend: 'T+1 更新' }
    ]
  },
  group: {
    key: 'group',
    title: '分组管理',
    eyebrow: 'Watchlist Groups',
    description: '把自选基金按策略或关注场景分组，降低高频查看时的寻找成本。',
    searchPlaceholder: '筛选分组或基金',
    primaryAction: '新建分组',
    secondaryAction: '管理标签',
    panelTitle: '自选分组',
    panelDescription: '分组入口先保留结构，后续可绑定标签、关注列表和批量导入。',
    viewType: 'group',
    metrics: [
      { label: '分组数量', value: '8', trend: '+1 本周' },
      { label: '自选基金', value: '36', trend: '12 个更新中' },
      { label: '标签覆盖', value: '92%', trend: '4 只待归类' }
    ]
  }
}

const activeMenuKey = ref('estimate')
const filterKeyword = ref('')
const asideMenuItems = ref<AsideMenuItem[]>(fallbackMenus)
const isMenuLoading = ref(false)
const menuLoadError = ref('')
const menuSyncedAt = ref('--')

const estimateRows: FundEstimateRow[] = [
  { code: '000001', name: '华夏成长混合', estimate: '1.3280', rate: '+1.2400%', time: '14:58:12' },
  { code: '110022', name: '易方达消费行业', estimate: '3.6412', rate: '-0.3200%', time: '14:58:09' },
  { code: '161725', name: '招商中证白酒指数', estimate: '0.9821', rate: '+0.5800%', time: '14:58:06' },
  { code: '320007', name: '诺安成长混合', estimate: '1.7624', rate: '+2.1600%', time: '14:57:58' }
]

const holdingRows = [
  { code: '110022', name: '易方达消费行业', risk: '中高波动', amount: '68,420.00', profit: '+3,214.50' },
  { code: '161725', name: '招商中证白酒指数', risk: '行业集中', amount: '42,180.55', profit: '-528.20' },
  { code: '000001', name: '华夏成长混合', risk: '均衡配置', amount: '36,840.00', profit: '+940.12' }
]

const tradeRecords = [
  { id: 1, time: '05-08 14:20', action: '追加申购', detail: '华夏成长混合 · 2,000.00 元' },
  { id: 2, time: '05-07 09:42', action: '定投扣款', detail: '易方达消费行业 · 800.00 元' },
  { id: 3, time: '05-06 15:02', action: '部分赎回', detail: '招商中证白酒指数 · 1,200.00 份' }
]

const groupRows = [
  { name: '午盘重点', count: 8, description: '临近午盘重点观察涨跌变化' },
  { name: '长期定投', count: 12, description: '低频维护，关注净值趋势' },
  { name: '行业主题', count: 9, description: '消费、科技、医药等主题池' },
  { name: '待确认', count: 4, description: '交易后等待份额确认' }
]

const normalizeMenuKey = (menu: MenuTreeNode): string => {
  const source = `${menu.path || ''} ${menu.component || ''} ${menu.permission || ''} ${menu.title}`

  if (/holding|持仓|仓位/.test(source)) {
    return 'holding'
  }

  if (/record|trade|交易|流水/.test(source)) {
    return 'record'
  }

  if (/group|tag|分组|标签/.test(source)) {
    return 'group'
  }

  return 'estimate'
}

const flattenVisibleMenus = (menus: MenuTreeNode[]): MenuTreeNode[] => {
  return menus.flatMap((menu) => {
    const children = flattenVisibleMenus(menu.children || [])
    const isVisible = menu.visible !== false && menu.status !== 0
    return isVisible ? [menu, ...children] : children
  })
}

const toAsideMenuItem = (menu: MenuTreeNode): AsideMenuItem => {
  const key = normalizeMenuKey(menu)
  const fallback = contentMap[key]

  return {
    key,
    title: menu.title || fallback.title,
    description: fallback.panelTitle,
    shortLabel: (menu.title || fallback.title).slice(0, 1),
    badge: key === 'estimate' ? '实时' : undefined
  }
}

const dedupeMenuItems = (items: AsideMenuItem[]): AsideMenuItem[] => {
  const map = new Map<string, AsideMenuItem>()

  for (const item of items) {
    if (!map.has(item.key)) {
      map.set(item.key, item)
    }
  }

  return Array.from(map.values())
}

const activeContent = computed(() => {
  return contentMap[activeMenuKey.value] || contentMap.estimate
})

const menuStatusText = computed(() => {
  return menuLoadError.value ? '已使用本地兜底菜单' : `上次同步 ${menuSyncedAt.value}`
})

const footerStatusText = computed(() => {
  return menuLoadError.value ? '系统状态：菜单接口未连接，当前为兜底结构' : '系统状态：菜单与页面结构正常'
})

const filteredEstimateRows = computed(() => {
  const keyword = filterKeyword.value.trim().toLowerCase()

  if (!keyword) {
    return estimateRows
  }

  return estimateRows.filter((row) => {
    return row.code.includes(keyword) || row.name.toLowerCase().includes(keyword)
  })
})

const handleMenuSelect = (key: string): void => {
  activeMenuKey.value = key
  filterKeyword.value = ''
}

const refreshView = (): void => {
  menuSyncedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
}

const loadMenus = async (): Promise<void> => {
  isMenuLoading.value = true
  menuLoadError.value = ''

  try {
    const menus = await fetchPortalMenus()
    const nextMenus = dedupeMenuItems(flattenVisibleMenus(menus).map(toAsideMenuItem))
    asideMenuItems.value = nextMenus.length > 0 ? nextMenus : fallbackMenus
    activeMenuKey.value = asideMenuItems.value[0]?.key || 'estimate'
    menuSyncedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch {
    asideMenuItems.value = fallbackMenus
    activeMenuKey.value = 'estimate'
    menuLoadError.value = '后端菜单暂不可用，已显示本地菜单'
    menuSyncedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } finally {
    isMenuLoading.value = false
  }
}

onMounted(() => {
  void loadMenus()
})
</script>

<style scoped lang="scss">
.fund-platform {
  min-height: 100vh;
  color: var(--text-color);
  background: var(--bg-gradient);
}

.fund-platform__body {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
  width: min(1440px, calc(100% - 32px));
  margin: 24px auto 0;
}

.fund-platform__main {
  display: grid;
  gap: 20px;
  min-width: 0;
  padding-bottom: 24px;
}

.info-hero,
.metric-card,
.data-panel {
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(18px);
}

.info-hero,
.data-panel {
  border-radius: 18px;
  background: var(--card-bg);
}

.info-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  align-items: center;
  gap: 24px;
  padding: 28px;

  h1 {
    margin: 0;
    font-size: 30px;
    line-height: 1.2;
  }

  p {
    max-width: 62ch;
    margin: 8px 0 0;
    color: var(--text-muted);
  }
}

.info-hero__eyebrow {
  color: var(--primary-color) !important;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.2;
}

.info-hero__actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  display: grid;
  gap: 8px;
  min-height: 118px;
  padding: 18px;
  border-radius: 16px;
  background: var(--card-bg);

  span,
  small {
    color: var(--text-muted);
  }

  strong {
    color: var(--text-color);
    font-size: 24px;
    line-height: 1.15;
  }

  small {
    color: #00a870;
    font-weight: 800;
  }
}

.metric-card__trend--down {
  color: #ff4d4f !important;
}

.data-panel {
  min-height: 390px;
  padding: 22px;
}

.data-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  h2 {
    margin: 0;
    font-size: 20px;
    line-height: 1.2;
  }

  p {
    max-width: 66ch;
    margin: 6px 0 0;
    color: var(--text-muted);
  }
}

.theme-table {
  border-radius: 14px;
  overflow: hidden;
}

.rate-text {
  font-weight: 800;
}

.rate-text--up {
  color: #00a870;
}

.rate-text--down {
  color: #ff4d4f;
}

.holding-list,
.timeline-list,
.group-grid {
  display: grid;
  gap: 10px;
}

.holding-row,
.timeline-row,
.group-card {
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: var(--button-bg);
}

.holding-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  min-height: 72px;
  padding: 14px 16px;

  div {
    display: grid;
    gap: 6px;
    min-width: 0;
  }

  div:last-child {
    text-align: right;
  }

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    color: var(--text-muted);
    font-size: 13px;
  }
}

.timeline-row {
  display: grid;
  grid-template-columns: 110px 120px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  min-height: 58px;
  padding: 13px 16px;

  span,
  small {
    color: var(--text-muted);
  }

  small {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.group-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.group-card {
  display: grid;
  gap: 8px;
  min-height: 116px;
  padding: 16px;

  strong {
    font-size: 16px;
  }

  span {
    color: var(--primary-color);
    font-weight: 800;
  }

  small {
    color: var(--text-muted);
    line-height: 1.5;
  }
}

:deep(.el-input__wrapper) {
  height: 44px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 13px;
  box-shadow: none;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: rgba(var(--primary-color-rgb), 0.42);
  box-shadow: 0 0 0 4px var(--focus-ring);
}

:deep(.el-input__inner) {
  color: var(--text-color);
}

:deep(.el-button) {
  height: 44px;
  border-color: var(--border-color-strong);
  border-radius: 12px;
  color: var(--text-color);
  background: var(--button-bg);
  font-weight: 800;
}

:deep(.el-button:hover) {
  border-color: rgba(var(--primary-color-rgb), 0.36);
  color: var(--text-color);
  background: var(--hover-bg);
}

:deep(.el-button--primary) {
  border-color: rgba(var(--primary-color-rgb), 0.42);
  color: var(--bg-color);
  background: var(--primary-color);
  box-shadow: 0 10px 20px rgba(var(--primary-color-rgb), 0.24);
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: var(--table-row-bg);
  --el-table-header-bg-color: var(--table-header-bg);
  --el-table-row-hover-bg-color: var(--table-row-hover-bg);
  --el-table-border-color: var(--border-color);
  color: var(--text-color);
  background: transparent;
}

:deep(.el-table th.el-table__cell),
:deep(.el-table tr),
:deep(.el-table td.el-table__cell) {
  color: var(--text-color);
  background: transparent;
}

@media (max-width: 960px) {
  .fund-platform__body {
    grid-template-columns: 1fr;
  }

  .info-hero {
    grid-template-columns: 1fr;
  }

  .info-hero__actions {
    width: 100%;
  }
}

@media (max-width: 720px) {
  .metric-grid,
  .group-grid {
    grid-template-columns: 1fr;
  }

  .data-panel__header,
  .info-hero__actions {
    grid-template-columns: 1fr;
  }

  .data-panel__header {
    flex-direction: column;
  }

  .holding-row,
  .timeline-row {
    grid-template-columns: 1fr;
  }

  .holding-row div:last-child {
    text-align: left;
  }
}
</style>
