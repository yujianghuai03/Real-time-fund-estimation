<template>
  <div class="fund-platform theme-transition">
    <AppHeader />

    <div class="fund-platform__body">
      <aside class="fund-platform__aside">
        <div class="aside-section">
          <span class="aside-section__label">Navigation</span>
          <button
            v-for="item in menuItems"
            :key="item"
            class="aside-section__item"
            :class="{ 'aside-section__item--active': item === activeMenu }"
            type="button"
            @click="activeMenu = item"
          >
            {{ item }}
          </button>
        </div>
      </aside>

      <main class="fund-platform__main">
        <section class="hero-card">
          <div>
            <p class="hero-card__eyebrow">Realtime Fund Estimation</p>
            <h1>基金数据平台</h1>
            <p>统一监控估值、涨跌幅、仓位变化与实时刷新状态。</p>
          </div>
          <div class="hero-card__actions">
            <el-input v-model="filterKeyword" placeholder="筛选基金代码" clearable />
            <el-button type="primary">刷新估值</el-button>
          </div>
        </section>

        <section class="metric-grid">
          <article v-for="metric in metrics" :key="metric.label" class="metric-card">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <small>{{ metric.trend }}</small>
          </article>
        </section>

        <section class="data-card">
          <div class="data-card__header">
            <div>
              <h2>实时估值列表</h2>
              <p>表格、卡片、按钮和输入框均跟随全局主题变量变化。</p>
            </div>
            <el-button>导出数据</el-button>
          </div>

          <el-table :data="tableData" class="theme-table" height="260">
            <el-table-column prop="code" label="基金代码" width="120" />
            <el-table-column prop="name" label="基金名称" min-width="160" />
            <el-table-column prop="estimate" label="估值" width="120" />
            <el-table-column prop="rate" label="涨跌幅" width="120" />
            <el-table-column prop="time" label="更新时间" width="160" />
          </el-table>
        </section>
      </main>
    </div>

    <footer class="fund-platform__footer">
      <span>系统状态：实时通道正常</span>
      <span>Theme Variables Ready</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

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

const activeMenu = ref('实时估值')
const filterKeyword = ref('')

const menuItems = ['实时估值', '持仓分析', '交易记录', '分组管理']

const metrics: MetricItem[] = [
  { label: '总资产估值', value: '256,840.1290', trend: '+2.3400%' },
  { label: '今日收益', value: '1,248.3700', trend: '+0.8600%' },
  { label: '关注基金', value: '36', trend: '12 个更新中' },
]

const tableData: FundEstimateRow[] = [
  { code: '000001', name: '华夏成长混合', estimate: '1.3280', rate: '+1.2400%', time: '14:58:12' },
  { code: '110022', name: '易方达消费行业', estimate: '3.6412', rate: '-0.3200%', time: '14:58:09' },
  { code: '161725', name: '招商中证白酒指数', estimate: '0.9821', rate: '+0.5800%', time: '14:58:06' },
  { code: '320007', name: '诺安成长混合', estimate: '1.7624', rate: '+2.1600%', time: '14:57:58' },
]
</script>

<style scoped lang="scss">
.fund-platform {
  min-height: 100vh;
  color: var(--text-color);
  background: var(--bg-gradient);
}

.fund-platform__body {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 20px;
  width: min(1440px, calc(100% - 32px));
  margin: 24px auto 0;
}

.fund-platform__aside,
.hero-card,
.metric-card,
.data-card,
.fund-platform__footer {
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(18px);
}

.fund-platform__aside {
  min-height: 520px;
  padding: 18px;
  border-radius: 18px;
  background: var(--aside-bg);
}

.aside-section {
  display: grid;
  gap: 10px;
}

.aside-section__label {
  color: var(--text-subtle);
  font-size: 12px;
}

.aside-section__item {
  width: 100%;
  height: 42px;
  border: 1px solid transparent;
  border-radius: 12px;
  color: var(--text-muted);
  text-align: left;
  background: transparent;
  cursor: pointer;
  padding: 0 14px;
  transition:
    background-color 0.3s ease,
    color 0.3s ease,
    border-color 0.3s ease;

  &:hover,
  &--active {
    color: var(--text-color);
    background: var(--hover-bg);
    border-color: var(--border-color-strong);
  }
}

.fund-platform__main {
  display: grid;
  gap: 20px;
  min-width: 0;
  padding-bottom: 24px;
  background: var(--main-bg);
  border-radius: 20px;
}

.hero-card,
.data-card {
  border-radius: 18px;
  background: var(--card-bg);
}

.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;

  h1 {
    margin: 0;
    font-size: 30px;
    line-height: 1.2;
  }

  p {
    margin: 8px 0 0;
    color: var(--text-muted);
  }
}

.hero-card .hero-card__eyebrow {
  color: var(--primary-color);
  font-size: 12px;
}

.hero-card__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 360px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  display: grid;
  gap: 8px;
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
  }
}

.data-card {
  padding: 22px;
}

.data-card__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  h2 {
    margin: 0;
    font-size: 20px;
  }

  p {
    margin: 6px 0 0;
    color: var(--text-muted);
  }
}

.theme-table {
  border-radius: 14px;
  overflow: hidden;
}

.fund-platform__footer {
  display: flex;
  justify-content: space-between;
  width: min(1440px, calc(100% - 32px));
  margin: 0 auto 20px;
  padding: 14px 18px;
  border-radius: 16px;
  color: var(--text-muted);
  background: var(--footer-bg);
}

:deep(.el-input__wrapper) {
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: var(--text-color);
}

:deep(.el-button) {
  border-color: var(--border-color-strong);
  color: var(--text-color);
  background: var(--button-bg);
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

  .fund-platform__aside {
    min-height: auto;
  }

  .hero-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-card__actions {
    width: 100%;
    min-width: 0;
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .hero-card__actions,
  .data-card__header,
  .fund-platform__footer {
    flex-direction: column;
  }
}
</style>
