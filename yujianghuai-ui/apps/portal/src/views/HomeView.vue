<template>
  <main class="fund-workbench">
    <header class="app-header">
      <router-link class="brand" to="/" aria-label="基金实时预估首页">
        <span class="brand-mark">Y</span>
        <span>
          <strong>基金实时预估</strong>
          <small>持仓估值工作台</small>
        </span>
      </router-link>

      <nav class="top-nav" aria-label="页面导航">
        <button
          v-for="item in navItems"
          :key="item.key"
          :class="{ active: activeSection === item.key }"
          type="button"
          @click="scrollToSection(item.key)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="header-actions">
        <el-tag :type="marketOpen ? 'success' : 'info'" effect="plain">
          {{ marketOpen ? '交易时段' : '非交易时段' }}
        </el-tag>
        <span class="last-refresh">最后刷新 {{ lastUpdated || '-' }}</span>
        <el-tooltip content="手动刷新数据" placement="bottom">
          <el-button :loading="refreshing" :icon="RefreshRight" circle aria-label="手动刷新数据" @click="loadWatchlist(true)" />
        </el-tooltip>
        <el-popover placement="bottom-end" trigger="hover" :width="300">
          <template #reference>
            <button class="user-trigger" type="button">
              <el-avatar :size="32">{{ avatarText }}</el-avatar>
              <span>
                <small>{{ authenticated ? '当前用户' : '未登录' }}</small>
                <strong>{{ displayName }}</strong>
              </span>
              <el-icon><ArrowDown /></el-icon>
            </button>
          </template>

          <div class="user-popover">
            <div class="user-popover-head">
              <strong>{{ displayName }}</strong>
              <el-tag size="small">{{ primaryRoleLabel }}</el-tag>
            </div>
            <template v-if="authenticated">
              <p>角色：{{ displayMeta }}</p>
              <p>租户：{{ tenantLabel }}</p>
              <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
            </template>
            <template v-else>
              <p>登录后可保存自选基金、维护持有金额并查看个人估值。</p>
              <el-button type="primary" @click="goLogin">前往登录</el-button>
            </template>
          </div>
        </el-popover>
      </div>
    </header>

    <section class="page-toolbar">
      <div>
        <p class="eyebrow">实时估值</p>
        <h1>我的基金工作台</h1>
        <p>集中查看组合盈亏、数据新鲜度和自选持仓，自动刷新会在页面隐藏时暂停。</p>
      </div>
      <div class="toolbar-controls">
        <el-segmented v-model="refreshMode" :options="refreshModeOptions" />
        <el-button :icon="Link" plain @click="openRepository">代码仓库</el-button>
      </div>
    </section>

    <el-alert
      v-if="unauthorized"
      class="status-alert"
      title="请先登录后查看当前用户保存的自选基金。"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-alert
      v-else-if="loadError"
      class="status-alert"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
    >
      <template #default>
        <el-button size="small" type="danger" plain @click="loadWatchlist(true)">重新加载</el-button>
      </template>
    </el-alert>

    <section id="overview" class="summary-grid" aria-label="组合摘要">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong :class="metric.tone">{{ metric.value }}</strong>
        <small>{{ metric.hint }}</small>
      </article>
    </section>

    <section class="refresh-panel" aria-label="刷新状态">
      <div>
        <el-icon :class="{ spinning: refreshing }"><Refresh /></el-icon>
        <strong>{{ refreshing ? '正在刷新估值' : '估值数据已就绪' }}</strong>
        <span>{{ refreshStatusText }}</span>
      </div>
      <el-tag :type="documentVisible ? 'success' : 'info'" effect="plain">
        {{ documentVisible ? '页面可见，轮询启用' : '页面隐藏，轮询暂停' }}
      </el-tag>
    </section>

    <section id="portfolio" class="workspace-section">
      <div class="section-head">
        <div>
          <h2>自选持仓</h2>
          <p>搜索基金并录入持有金额，表格会根据实时估值计算预估盈亏。</p>
        </div>
        <el-button :icon="Setting" plain @click="settingsVisible = true">页面设置</el-button>
      </div>

      <div class="watch-toolbar">
        <el-select
          v-model="selectedCode"
          filterable
          remote
          clearable
          reserve-keyword
          placeholder="搜索基金代码或名称"
          :remote-method="remoteSearch"
          :loading="searching"
        >
          <el-option
            v-for="item in searchOptions"
            :key="item.code"
            :label="`${item.code} ${item.name}`"
            :value="item.code"
          >
            <div class="fund-option">
              <strong>{{ item.code }} {{ item.name }}</strong>
              <span>{{ item.type || '基金' }} / {{ item.company || '未知公司' }}</span>
            </div>
          </el-option>
        </el-select>
        <el-input-number v-model="newHoldingAmount" :min="0" :precision="2" :step="1000" />
        <el-button type="primary" :loading="adding" @click="addSelectedFund">添加自选</el-button>
      </div>

      <el-skeleton v-if="initialLoading" :rows="6" animated />

      <template v-else>
        <el-empty v-if="!watchlist.length" description="暂无自选基金，搜索基金后添加到工作台。" />

        <el-table v-else :data="watchlist" class="fund-table" row-key="code" @row-click="openTrend">
          <el-table-column prop="code" label="代码" width="100" fixed />
          <el-table-column prop="name" label="基金名称" min-width="220" />
          <el-table-column label="持有金额" width="170">
            <template #default="{ row }">
              <el-input-number
                v-model="row.holdingAmount"
                :min="0"
                :precision="2"
                :step="1000"
                controls-position="right"
                @change="() => saveHolding(row)"
                @click.stop
              />
            </template>
          </el-table-column>
          <el-table-column label="昨日净值" width="110">
            <template #default="{ row }">{{ formatNumber(row.previousNav) }}</template>
          </el-table-column>
          <el-table-column label="预估净值" width="110">
            <template #default="{ row }">{{ formatNumber(row.estimateNav) }}</template>
          </el-table-column>
          <el-table-column label="涨跌幅" width="110">
            <template #default="{ row }">
              <span class="rate-cell" :class="toneClass(row.estimateRate || 0)">
                <el-icon><component :is="(row.estimateRate || 0) >= 0 ? TopRight : BottomRight" /></el-icon>
                {{ formatPercent(row.estimateRate || 0) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="预估盈亏" width="130">
            <template #default="{ row }">
              <strong :class="toneClass(row.estimateProfit || 0)">{{ formatMoney(row.estimateProfit || 0) }}</strong>
            </template>
          </el-table-column>
          <el-table-column label="预估市值" width="140">
            <template #default="{ row }">{{ formatMoney(row.estimateMarketValue || row.holdingAmount || 0) }}</template>
          </el-table-column>
          <el-table-column label="估值时间" min-width="170">
            <template #default="{ row }">
              <span v-if="row.error" class="error-text">{{ row.error }}</span>
              <span v-else>{{ row.estimateTime || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openTrend(row)">详情</el-button>
              <el-button link type="danger" @click.stop="removeFund(row.code)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mobile-list">
          <article v-for="row in watchlist" :key="row.code" class="fund-mobile-card" @click="openTrend(row)">
            <div>
              <strong>{{ row.name }}</strong>
              <span>{{ row.code }} / {{ row.estimateTime || '-' }}</span>
            </div>
            <b :class="toneClass(row.estimateProfit || 0)">{{ formatMoney(row.estimateProfit || 0) }}</b>
            <dl>
              <div>
                <dt>持有金额</dt>
                <dd>{{ formatMoney(row.holdingAmount || 0) }}</dd>
              </div>
              <div>
                <dt>涨跌幅</dt>
                <dd :class="toneClass(row.estimateRate || 0)">{{ formatPercent(row.estimateRate || 0) }}</dd>
              </div>
              <div>
                <dt>预估净值</dt>
                <dd>{{ formatNumber(row.estimateNav) }}</dd>
              </div>
            </dl>
          </article>
        </div>
      </template>
    </section>

    <section id="notice" class="notice-section">
      <div>
        <h2>估值说明</h2>
        <p>实时估值由后端从公开基金数据源获取，仅供盘中参考。最终净值、份额和收益以基金公司披露与交易确认结果为准。</p>
      </div>
      <div class="notice-actions">
        <el-button type="primary" plain @click="openFeedback">提交反馈</el-button>
        <el-button type="warning" plain @click="coffeeDialogVisible = true">支持作者</el-button>
      </div>
    </section>

    <el-drawer v-model="trendVisible" title="持仓详情" size="420px">
      <template v-if="selectedRow">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="基金">{{ selectedRow.code }} {{ selectedRow.name }}</el-descriptions-item>
          <el-descriptions-item label="持有金额">{{ formatMoney(selectedRow.holdingAmount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="预估市值">{{ formatMoney(selectedRow.estimateMarketValue || selectedRow.holdingAmount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="预估盈亏">
            <span :class="toneClass(selectedRow.estimateProfit || 0)">{{ formatMoney(selectedRow.estimateProfit || 0) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="涨跌幅">
            <span :class="toneClass(selectedRow.estimateRate || 0)">{{ formatPercent(selectedRow.estimateRate || 0) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="数据时间">{{ selectedRow.estimateTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-alert
          class="drawer-note"
          title="历史走势接口尚未接入，当前展示最近一次估值快照。"
          type="info"
          show-icon
          :closable="false"
        />
      </template>
    </el-drawer>

    <el-dialog v-model="settingsVisible" title="页面设置" width="420px" align-center>
      <el-form label-width="110px">
        <el-form-item label="刷新模式">
          <el-segmented v-model="refreshMode" :options="refreshModeOptions" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="documentVisible ? 'success' : 'info'">
            {{ documentVisible ? '页面可见' : '页面隐藏' }}
          </el-tag>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog v-model="coffeeDialogVisible" title="请作者喝咖啡" width="420px" align-center destroy-on-close>
      <div class="coffee-dialog">
        <el-radio-group v-model="payMethod">
          <el-radio-button label="alipay">支付宝</el-radio-button>
          <el-radio-button label="wechat">微信支付</el-radio-button>
        </el-radio-group>
        <img :src="currentPayImage" :alt="payMethod === 'alipay' ? '支付宝收款码' : '微信收款码'" />
        <p>感谢支持，维护一个实时估值小工具需要一点耐心，也需要一点咖啡因。</p>
      </div>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  BottomRight,
  Link,
  Refresh,
  RefreshRight,
  Setting,
  TopRight
} from '@element-plus/icons-vue'
import alipayPay from '../assets/alipay-pay.png'
import wechatPay from '../assets/wechat-pay.png'
import { getUserInfo, logout, type UserInfo } from '../api/auth'
import {
  addWatchFund,
  deleteWatchFund,
  listWatchFunds,
  searchFunds,
  updateFundHolding,
  type FundEstimateRow,
  type FundSearchItem
} from '../api/fund'

type PayMethod = 'alipay' | 'wechat'
type RefreshMode = 'manual' | 'standard' | 'fast'

const repositoryUrl = 'https://github.com/yujianghuai03/yujianghuai'
const feedbackUrl = `${repositoryUrl}/issues/new`
const roleTextMap: Record<string, string> = {
  ROLE_ADMIN: '管理员',
  ROLE_USER: '普通用户',
  ROLE_GUEST: '访客',
  ROLE_MANAGER: '经理',
  ROLE_OPERATOR: '运营人员'
}
const navItems = [
  { key: 'overview', label: '总览' },
  { key: 'portfolio', label: '自选持仓' },
  { key: 'notice', label: '说明' }
]
const refreshModeOptions = [
  { label: '手动', value: 'manual' },
  { label: '标准 15s', value: 'standard' },
  { label: '快速 5s', value: 'fast' }
]

const router = useRouter()
const activeSection = ref('overview')
const selectedCode = ref('')
const searching = ref(false)
const refreshing = ref(false)
const initialLoading = ref(true)
const adding = ref(false)
const unauthorized = ref(false)
const loadError = ref('')
const authenticated = ref(hasToken())
const coffeeDialogVisible = ref(false)
const settingsVisible = ref(false)
const trendVisible = ref(false)
const documentVisible = ref(document.visibilityState === 'visible')
const refreshMode = ref<RefreshMode>('standard')
const payMethod = ref<PayMethod>('alipay')
const searchOptions = ref<FundSearchItem[]>([])
const watchlist = ref<FundEstimateRow[]>([])
const lastUpdated = ref('')
const newHoldingAmount = ref(10000)
const userInfo = ref<UserInfo | null>(null)
const selectedRow = ref<FundEstimateRow | null>(null)
let searchTimer: number | undefined
let refreshTimer: number | undefined
let controller: AbortController | null = null

const refreshMs = computed(() => {
  if (refreshMode.value === 'fast') {
    return 5000
  }
  if (refreshMode.value === 'manual') {
    return 0
  }
  return 15000
})
const totalHolding = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.holdingAmount || 0), 0))
const totalProfit = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.estimateProfit || 0), 0))
const totalMarketValue = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.estimateMarketValue || item.holdingAmount || 0), 0))
const portfolioChange = computed(() => totalHolding.value ? totalProfit.value / totalHolding.value * 100 : 0)
const staleSeconds = computed(() => {
  if (!lastUpdated.value) {
    return null
  }
  const parsed = Date.parse(lastUpdated.value)
  return Number.isNaN(parsed) ? null : Math.max(0, Math.round((Date.now() - parsed) / 1000))
})
const marketOpen = computed(() => {
  const now = new Date()
  const day = now.getDay()
  const minutes = now.getHours() * 60 + now.getMinutes()
  return day >= 1 && day <= 5 && minutes >= 9 * 60 + 30 && minutes <= 15 * 60
})
const metrics = computed(() => [
  { label: '今日预估盈亏', value: formatMoney(totalProfit.value), hint: '按持仓金额与实时涨跌估算', tone: toneClass(totalProfit.value) },
  { label: '预估总市值', value: formatMoney(totalMarketValue.value), hint: `${watchlist.value.length} 只自选基金`, tone: '' },
  { label: '持有金额', value: formatMoney(totalHolding.value), hint: '用户录入的本金口径', tone: '' },
  { label: '组合涨跌', value: formatPercent(portfolioChange.value), hint: '预估盈亏 / 持有金额', tone: toneClass(portfolioChange.value) },
  { label: '数据新鲜度', value: staleSeconds.value === null ? '-' : `${staleSeconds.value}s`, hint: lastUpdated.value || '等待首次刷新', tone: staleSeconds.value !== null && staleSeconds.value > 60 ? 'down' : 'up' }
])
const refreshStatusText = computed(() => {
  if (refreshMode.value === 'manual') {
    return '当前为手动刷新模式。'
  }
  if (!documentVisible.value) {
    return '页面不可见，已暂停自动请求。'
  }
  return `自动刷新周期 ${refreshMs.value / 1000} 秒。`
})
const displayName = computed(() => userInfo.value?.username || getStoredValue('YJH_USERNAME') || '未登录')
const tenantLabel = computed(() => userInfo.value?.tenant_name || getStoredValue('YJH_TENANT_NAME') || '-')
const roleLabels = computed(() => {
  const authorities = Array.isArray(userInfo.value?.authorities) && userInfo.value.authorities.length
    ? userInfo.value.authorities
    : []
  return authorities.length ? authorities.map((role) => roleTextMap[role] || role.replace(/^ROLE_/, '')) : ['基金观察者']
})
const primaryRoleLabel = computed(() => roleLabels.value[0] || '基金观察者')
const displayMeta = computed(() => roleLabels.value.join(' / '))
const avatarText = computed(() => displayName.value === '未登录' ? '未' : displayName.value.trim().slice(0, 1).toUpperCase())
const currentPayImage = computed(() => payMethod.value === 'alipay' ? alipayPay : wechatPay)

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  void loadUserProfile()
  void loadWatchlist(true)
})

onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.clearTimeout(searchTimer)
  stopPolling()
})

watch(refreshMs, () => schedulePolling())

function hasToken() {
  return Boolean(getStoredValue('YJH_TOKEN'))
}

function getStoredValue(key: string) {
  if (typeof window === 'undefined') {
    return ''
  }
  return window.localStorage.getItem(key) ?? ''
}

function clearAuthStorage() {
  localStorage.removeItem('YJH_TOKEN')
  localStorage.removeItem('YJH_TENANT_ID')
  localStorage.removeItem('YJH_TENANT_NAME')
  localStorage.removeItem('YJH_USERNAME')
  localStorage.removeItem('YJH_ADMIN_AUTH')
}

function handleVisibilityChange() {
  documentVisible.value = document.visibilityState === 'visible'
  if (documentVisible.value) {
    void loadWatchlist(false)
  } else {
    stopPolling()
  }
}

function schedulePolling() {
  stopPolling()
  if (!refreshMs.value || !documentVisible.value || unauthorized.value) {
    return
  }
  refreshTimer = window.setTimeout(() => void loadWatchlist(false), refreshMs.value)
}

function stopPolling() {
  window.clearTimeout(refreshTimer)
  refreshTimer = undefined
  controller?.abort()
  controller = null
}

async function loadUserProfile() {
  authenticated.value = hasToken()
  if (!authenticated.value) {
    userInfo.value = null
    return
  }
  try {
    userInfo.value = await getUserInfo()
    if (userInfo.value?.username) {
      localStorage.setItem('YJH_USERNAME', userInfo.value.username)
    }
    if (userInfo.value?.tenant_name) {
      localStorage.setItem('YJH_TENANT_NAME', userInfo.value.tenant_name)
    }
  } catch (error) {
    if (isUnauthorized(error)) {
      clearAuthStorage()
      authenticated.value = false
      userInfo.value = null
      return
    }
    ElMessage.warning(error instanceof Error ? error.message : '个人信息加载失败')
  }
}

function remoteSearch(keyword: string) {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(async () => {
    if (!keyword.trim()) {
      searchOptions.value = []
      return
    }
    searching.value = true
    try {
      searchOptions.value = await searchFunds(keyword)
    } catch (error) {
      handleError(error, '基金搜索失败')
    } finally {
      searching.value = false
    }
  }, 260)
}

async function addSelectedFund() {
  const option = searchOptions.value.find((item) => item.code === selectedCode.value)
  if (!option) {
    ElMessage.warning('请先选择基金')
    return
  }
  if (newHoldingAmount.value <= 0) {
    ElMessage.warning('持有金额必须大于 0')
    return
  }
  adding.value = true
  try {
    await addWatchFund(option.code, option.name, newHoldingAmount.value)
    selectedCode.value = ''
    searchOptions.value = []
    ElMessage.success('已添加自选基金')
    await loadWatchlist(true)
  } catch (error) {
    handleError(error, '添加基金失败')
  } finally {
    adding.value = false
  }
}

async function loadWatchlist(manual: boolean) {
  if (!documentVisible.value && !manual) {
    return
  }
  controller?.abort()
  controller = new AbortController()
  refreshing.value = true
  authenticated.value = hasToken()
  loadError.value = ''
  try {
    watchlist.value = await listWatchFunds(controller.signal)
    unauthorized.value = false
    lastUpdated.value = watchlist.value.find((item) => item.estimateTime)?.estimateTime
      || new Date().toLocaleString('zh-CN', { hour12: false })
  } catch (error) {
    if (isAbortError(error)) {
      return
    }
    if (isUnauthorized(error)) {
      unauthorized.value = true
      watchlist.value = []
      lastUpdated.value = ''
    } else {
      loadError.value = error instanceof Error ? error.message : '加载自选基金失败'
      if (manual) {
        handleError(error, '加载自选基金失败')
      }
    }
  } finally {
    refreshing.value = false
    initialLoading.value = false
    schedulePolling()
  }
}

async function saveHolding(row: FundEstimateRow) {
  try {
    await updateFundHolding(row.code, row.holdingAmount || 0)
    ElMessage.success('持有金额已保存')
    await loadWatchlist(true)
  } catch (error) {
    handleError(error, '保存持有金额失败')
  }
}

async function removeFund(code: string) {
  try {
    await ElMessageBox.confirm('确定删除这只自选基金吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteWatchFund(code)
    ElMessage.success('已删除自选基金')
    await loadWatchlist(true)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    handleError(error, '删除基金失败')
  }
}

async function handleLogout() {
  try {
    await logout()
  } catch (error) {
    if (!isUnauthorized(error)) {
      ElMessage.warning(error instanceof Error ? error.message : '退出登录时发生异常，已清理本地登录状态')
    }
  } finally {
    clearAuthStorage()
    authenticated.value = false
    userInfo.value = null
    selectedCode.value = ''
    searchOptions.value = []
    watchlist.value = []
    lastUpdated.value = ''
    unauthorized.value = true
    ElMessage.success('已退出登录')
  }
}

function openTrend(row: FundEstimateRow) {
  selectedRow.value = row
  trendVisible.value = true
}

function scrollToSection(id: string) {
  activeSection.value = id
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goLogin() {
  router.push('/login')
}

function openRepository() {
  window.open(repositoryUrl, '_blank', 'noopener,noreferrer')
}

function openFeedback() {
  window.open(feedbackUrl, '_blank', 'noopener,noreferrer')
}

function toneClass(value: number) {
  if (value > 0) {
    return 'up'
  }
  if (value < 0) {
    return 'down'
  }
  return ''
}

function formatPercent(value: number) {
  return `${value >= 0 ? '+' : ''}${Number(value || 0).toFixed(2)}%`
}

function formatNumber(value?: number) {
  return value === undefined || value === null ? '-' : Number(value).toFixed(4)
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 2
  }).format(Number(value || 0))
}

function isAbortError(error: unknown) {
  return typeof error === 'object'
    && error !== null
    && ('name' in error || 'code' in error)
    && ((error as { name?: string }).name === 'CanceledError' || (error as { code?: string }).code === 'ERR_CANCELED')
}

function isUnauthorized(error: unknown) {
  return typeof error === 'object'
    && error !== null
    && 'response' in error
    && (error as { response?: { status?: number } }).response?.status === 401
}

function handleError(error: unknown, fallback: string) {
  const message = error instanceof Error ? error.message : fallback
  ElMessage.error(message)
}
</script>
