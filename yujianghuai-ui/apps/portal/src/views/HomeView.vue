<template>
  <main class="fund-desk">
    <header class="fund-nav">
      <div class="brand-block">
        <router-link class="brand" to="/">
          <span class="brand-mark">Y</span>
          <span>基金实时预估</span>
        </router-link>
        <p class="brand-subtitle">实时查看自选基金估值、盈亏表现和持仓变化。</p>
      </div>

      <div class="nav-actions">
        <el-tooltip content="GitHub 仓库" placement="bottom">
          <a class="icon-action repo-icon-button" :href="repositoryUrl" target="_blank" rel="noreferrer" aria-label="GitHub 仓库">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M12 2C6.48 2 2 6.58 2 12.23c0 4.52 2.87 8.35 6.84 9.71.5.09.68-.22.68-.49 0-.24-.01-1.04-.01-1.88-2.78.62-3.37-1.21-3.37-1.21-.45-1.18-1.11-1.49-1.11-1.49-.91-.64.07-.63.07-.63 1 .08 1.53 1.05 1.53 1.05.9 1.57 2.35 1.12 2.92.85.09-.67.35-1.12.63-1.37-2.22-.26-4.55-1.14-4.55-5.06 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.31.1-2.72 0 0 .84-.28 2.75 1.05a9.3 9.3 0 0 1 5 0c1.9-1.33 2.74-1.05 2.74-1.05.55 1.41.2 2.46.1 2.72.64.72 1.03 1.63 1.03 2.75 0 3.93-2.33 4.79-4.56 5.05.36.32.68.95.68 1.92 0 1.39-.01 2.5-.01 2.84 0 .27.18.58.69.48A10.26 10.26 0 0 0 22 12.23C22 6.58 17.52 2 12 2Z"
              />
            </svg>
          </a>
        </el-tooltip>

        <el-tooltip content="刷新数据" placement="bottom">
          <el-button class="icon-action refresh-icon-button" type="primary" circle :loading="refreshing" @click="loadWatchlist">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
        </el-tooltip>

        <el-popover placement="bottom-end" trigger="hover" :width="300">
          <template #reference>
            <button class="user-trigger" type="button">
              <el-avatar class="user-avatar" :size="32">{{ avatarText }}</el-avatar>
              <span class="user-trigger-copy">
                <span class="user-caption">当前用户</span>
                <strong>{{ displayName }}</strong>
              </span>
              <el-icon class="user-trigger-arrow"><ArrowDown /></el-icon>
            </button>
          </template>

          <div class="user-popover">
            <div class="user-popover-head">
              <strong>{{ displayName }}</strong>
              <span class="user-role-badge">{{ primaryRoleLabel }}</span>
            </div>

            <template v-if="authenticated">
              <div class="user-info-list">
                <div class="user-info-item">
                  <span class="user-info-label">角色</span>
                  <strong>{{ displayMeta }}</strong>
                </div>
                <div class="user-info-item">
                  <span class="user-info-label">租户</span>
                  <strong>{{ tenantLabel }}</strong>
                </div>
              </div>
              <div class="user-popover-actions">
                <el-button type="danger" plain round @click="handleLogout">退出登录</el-button>
              </div>
            </template>

            <template v-else>
              <div class="user-info-empty">
                <p>当前还没有登录，登录后可查看角色和租户信息。</p>
                <el-button type="primary" round @click="goLogin">前往登录</el-button>
              </div>
            </template>
          </div>
        </el-popover>
      </div>
    </header>

    <section class="fund-hero">
      <div class="hero-copy">
        <span class="eyebrow">基金实时预估</span>
        <h1>我的基金<br /><span>实时预估</span></h1>
        <p>
          前端负责展示数据，后端会保存当前用户的自选基金，并在打开页面或每 10 秒刷新时实时拉取估值数据，帮助你更直观地观察组合波动。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="scrollToSection('watchlist')">查看自选基金</el-button>
          <el-button size="large" round plain @click="authenticated ? openFeedback() : goLogin()">
            {{ authenticated ? '点此提交反馈' : '先去登录' }}
          </el-button>
        </div>
      </div>

      <aside class="quote-board">
        <div class="quote-topline">
          <span>今日预估盈亏</span>
          <strong :class="totalProfit >= 0 ? 'up' : 'down'">{{ formatMoney(totalProfit) }}</strong>
        </div>
        <div class="nav-estimate">
          <span>预估总市值</span>
          <b>{{ formatMoney(totalMarketValue) }}</b>
        </div>
        <div class="summary-grid">
          <div>
            <span>持有金额</span>
            <b>{{ formatMoney(totalHolding) }}</b>
          </div>
          <div>
            <span>组合涨跌</span>
            <b :class="portfolioChange >= 0 ? 'up' : 'down'">{{ formatPercent(portfolioChange) }}</b>
          </div>
        </div>
        <div class="quote-meta">
          <span>更新时间 {{ lastUpdated || '-' }}</span>
          <span>自动刷新：10 秒</span>
        </div>
      </aside>
    </section>

    <section id="watchlist" class="watch-card">
      <div class="watch-toolbar">
        <div>
          <h2>用户自选基金</h2>
          <p>通过后端搜索基金并加入当前账号，可维护持有金额，系统会实时计算预估盈亏和市值。</p>
        </div>
        <div class="fund-search">
          <el-select
            v-model="selectedCode"
            filterable
            remote
            clearable
            reserve-keyword
            placeholder="搜索基金代码或名称"
            :remote-method="remoteSearch"
            :loading="searching"
            style="width: 320px"
          >
            <el-option
              v-for="item in searchOptions"
              :key="item.code"
              :label="`${item.code} ${item.name}`"
              :value="item.code"
            >
              <div class="fund-option">
                <strong>{{ item.code }} {{ item.name }}</strong>
                <span>{{ item.type || '基金' }} · {{ item.company || '未知公司' }}</span>
              </div>
            </el-option>
          </el-select>
          <el-input-number v-model="newHoldingAmount" :min="0" :precision="2" :step="1000" />
          <el-button type="primary" round :loading="adding" @click="addSelectedFund">添加</el-button>
        </div>
      </div>

      <el-alert
        v-if="unauthorized"
        title="请先登录后查看当前用户保存的自选基金。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-table :data="watchlist" class="fund-table" row-key="code" empty-text="暂无自选基金">
        <el-table-column prop="code" label="代码" width="100" fixed />
        <el-table-column prop="name" label="基金名称" min-width="230" />
        <el-table-column label="持有金额" width="180">
          <template #default="{ row }">
            <el-input-number
              v-model="row.holdingAmount"
              :min="0"
              :precision="2"
              :step="1000"
              controls-position="right"
              @change="saveHolding(row)"
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
            <span :class="row.estimateRate >= 0 ? 'up' : 'down'">{{ formatPercent(row.estimateRate || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预估盈亏" width="130">
          <template #default="{ row }">
            <strong :class="row.estimateProfit >= 0 ? 'up' : 'down'">{{ formatMoney(row.estimateProfit || 0) }}</strong>
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
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeFund(row.code)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section id="risk" class="risk-panel">
      <strong>估算说明</strong>
      <p>实时估值由后端从公开基金数据源获取，仅供盘中参考。最终净值和收益以基金公司披露为准。</p>
    </section>

    <footer id="footer" class="site-footer">
      <div class="footer-card">
        <p>数据源：实时估值与重仓直连东方财富，仅供个人学习及参考使用。数据可能存在延迟，不作为任何投资建议。</p>
        <p>注：估算数据与真实结算数据会有 1% 左右误差，非股票型基金误差较大。</p>
        <div class="footer-links">
          <a class="footer-link" :href="feedbackUrl" target="_blank" rel="noreferrer">点此提交反馈</a>
          <el-button type="warning" round @click="coffeeDialogVisible = true">点此请作者喝杯咖啡</el-button>
        </div>
      </div>
    </footer>

    <el-dialog v-model="coffeeDialogVisible" title="请作者喝咖啡" width="420px" align-center destroy-on-close>
      <div class="coffee-dialog">
        <el-radio-group v-model="payMethod" class="coffee-switch">
          <el-radio-button label="alipay">支付宝</el-radio-button>
          <el-radio-button label="wechat">微信支付</el-radio-button>
        </el-radio-group>

        <div class="pay-preview">
          <img :src="currentPayImage" :alt="payMethod === 'alipay' ? '支付宝收款码' : '微信支付收款码'" />
        </div>

        <p class="coffee-note">感谢您的支持！您的鼓励是我持续维护和更新的动力。</p>
      </div>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, RefreshRight } from '@element-plus/icons-vue'
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

const repositoryUrl = 'https://github.com/yujianghuai03/yujianghuai'
const feedbackUrl = `${repositoryUrl}/issues/new`
const roleTextMap: Record<string, string> = {
  ROLE_ADMIN: '管理员',
  ROLE_USER: '普通用户',
  ROLE_GUEST: '访客',
  ROLE_MANAGER: '经理',
  ROLE_OPERATOR: '运营人员'
}

const router = useRouter()
const selectedCode = ref('')
const searching = ref(false)
const refreshing = ref(false)
const adding = ref(false)
const unauthorized = ref(false)
const authenticated = ref(hasToken())
const coffeeDialogVisible = ref(false)
const payMethod = ref<PayMethod>('alipay')
const searchOptions = ref<FundSearchItem[]>([])
const watchlist = ref<FundEstimateRow[]>([])
const lastUpdated = ref('')
const newHoldingAmount = ref(10000)
const userInfo = ref<UserInfo | null>(null)
let searchTimer: number | undefined
let refreshTimer: number | undefined

const totalHolding = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.holdingAmount || 0), 0))
const totalProfit = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.estimateProfit || 0), 0))
const totalMarketValue = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.estimateMarketValue || item.holdingAmount || 0), 0))
const portfolioChange = computed(() => totalHolding.value ? totalProfit.value / totalHolding.value * 100 : 0)
const displayName = computed(() => userInfo.value?.username || getStoredValue('YJH_USERNAME') || '未登录')
const tenantLabel = computed(() => userInfo.value?.tenant_name || getStoredValue('YJH_TENANT_NAME') || '-')
const roleLabels = computed(() => {
  const authorities = Array.isArray(userInfo.value?.authorities) && userInfo.value.authorities.length
    ? userInfo.value.authorities
    : []
  if (!authorities.length) {
    return ['基金观察者']
  }
  return authorities.map((role) => roleTextMap[role] || role.replace(/^ROLE_/, ''))
})
const primaryRoleLabel = computed(() => roleLabels.value[0] || '基金观察者')
const displayMeta = computed(() => roleLabels.value.join(' / '))
const avatarText = computed(() => {
  const name = displayName.value.trim()
  if (!name || name === '未登录') {
    return '未'
  }
  return name.slice(0, 1).toUpperCase()
})
const currentPayImage = computed(() => payMethod.value === 'alipay' ? alipayPay : wechatPay)

onMounted(() => {
  void loadUserProfile()
  void loadWatchlist()
  refreshTimer = window.setInterval(loadWatchlist, 10000)
})

onBeforeUnmount(() => {
  window.clearTimeout(searchTimer)
  window.clearInterval(refreshTimer)
})

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
  adding.value = true
  try {
    await addWatchFund(option.code, option.name, newHoldingAmount.value)
    selectedCode.value = ''
    await loadWatchlist()
  } catch (error) {
    handleError(error, '添加基金失败')
  } finally {
    adding.value = false
  }
}

async function loadWatchlist() {
  refreshing.value = true
  authenticated.value = hasToken()
  try {
    watchlist.value = await listWatchFunds()
    unauthorized.value = false
    lastUpdated.value = watchlist.value.find((item) => item.estimateTime)?.estimateTime
      || new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch (error) {
    if (isUnauthorized(error)) {
      unauthorized.value = true
      watchlist.value = []
      lastUpdated.value = ''
    } else {
      handleError(error, '加载自选基金失败')
    }
  } finally {
    refreshing.value = false
  }
}

async function saveHolding(row: FundEstimateRow) {
  try {
    await updateFundHolding(row.code, row.holdingAmount || 0)
    await loadWatchlist()
  } catch (error) {
    handleError(error, '保存持有金额失败')
  }
}

async function removeFund(code: string) {
  try {
    await deleteWatchFund(code)
    await loadWatchlist()
  } catch (error) {
    handleError(error, '删除基金失败')
  }
}

async function handleLogout() {
  try {
    await logout()
  } catch (error) {
    if (!isUnauthorized(error)) {
      ElMessage.warning(error instanceof Error ? error.message : '退出登录时发生异常，已为你清理本地登录状态')
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

function scrollToSection(id: string) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goLogin() {
  router.push('/login')
}

function openFeedback() {
  window.open(feedbackUrl, '_blank', 'noopener,noreferrer')
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
