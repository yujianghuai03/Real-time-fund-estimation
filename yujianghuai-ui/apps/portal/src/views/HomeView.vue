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

    <section class="index-strip" aria-label="市场指数">
      <article v-for="item in marketIndices" :key="item.code" class="index-card">
        <span>{{ item.name }}</span>
        <strong :class="toneClass(item.changeRate || 0)">{{ formatIndexPoint(item.price) }}</strong>
        <small :class="toneClass(item.changeRate || 0)">
          {{ formatSignedNumber(item.change || 0) }} / {{ formatPercent(item.changeRate || 0) }}
        </small>
      </article>
      <el-empty v-if="!marketIndices.length && !indicesLoading" description="指数数据暂不可用" />
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
          v-model="selectedCodes"
          multiple
          collapse-tags
          collapse-tags-tooltip
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
        <el-button type="primary" :loading="adding" @click="addSelectedFund">添加自选</el-button>
      </div>

      <div class="group-toolbar">
        <div class="group-tabs">
          <button
            v-for="group in groupTabs"
            :key="group.key"
            :class="{ active: activeGroupKey === group.key }"
            type="button"
            @click="activeGroupKey = group.key"
          >
            {{ group.name }}（{{ group.count }}）
          </button>
        </div>
        <div class="group-actions">
          <el-button :icon="Setting" plain @click="groupManageVisible = true">管理分组</el-button>
        </div>
      </div>

      <div class="sort-toolbar">
        <el-segmented v-model="sortKey" :options="sortOptions" />
        <el-button :icon="sortOrder === 'asc' ? TopRight : BottomRight" plain @click="toggleSortOrder">
          {{ sortOrder === 'asc' ? '升序' : '降序' }}
        </el-button>
      </div>

      <el-skeleton v-if="initialLoading" :rows="6" animated />

      <template v-else>
        <el-empty v-if="!filteredWatchlist.length" :description="emptyDescription" />

        <el-table v-else :data="sortedWatchlist" class="fund-table" row-key="code" @row-click="openTrend">
          <el-table-column prop="code" label="代码" width="100" fixed />
          <el-table-column prop="name" label="基金名称" min-width="220" />
          <el-table-column label="所属分组" min-width="220">
            <template #default="{ row }">
              <el-select
                v-model="row.groupIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                clearable
                placeholder="选择分组"
                @change="() => saveFundGroups(row)"
                @click.stop
              >
                <el-option
                  v-for="group in customGroups"
                  :key="group.id"
                  :label="group.name"
                  :value="group.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="持有金额" width="170">
            <template #default="{ row }">
              <div class="holding-cell">
                <span>{{ formatMoney(row.holdingAmount || 0) }}</span>
                <el-tooltip content="持仓操作" placement="top">
                  <el-button :icon="Setting" circle size="small" @click.stop="openHoldingAction(row)" />
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="成本净值" width="120">
            <template #default="{ row }">{{ formatNumber(row.holdingCostNav) }}</template>
          </el-table-column>
          <el-table-column label="持有份额" width="130">
            <template #default="{ row }">{{ formatShares(row.holdingShares) }}</template>
          </el-table-column>
          <el-table-column label="首次买入" width="130">
            <template #default="{ row }">{{ row.firstBuyDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="持有天数" width="110">
            <template #default="{ row }">{{ row.firstBuyDate ? `${daysFromDate(row.firstBuyDate)}天` : '-' }}</template>
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
          <article v-for="row in sortedWatchlist" :key="row.code" class="fund-mobile-card" @click="openTrend(row)">
            <div>
              <strong>{{ row.name }}</strong>
              <span>{{ row.code }} / {{ row.estimateTime || '-' }}</span>
            </div>
            <b :class="toneClass(row.estimateProfit || 0)">{{ formatMoney(row.estimateProfit || 0) }}</b>
            <el-select
              v-model="row.groupIds"
              multiple
              collapse-tags
              clearable
              placeholder="选择分组"
              @change="() => saveFundGroups(row)"
              @click.stop
            >
              <el-option
                v-for="group in customGroups"
                :key="group.id"
                :label="group.name"
                :value="group.id"
              />
            </el-select>
            <dl>
              <div>
                <dt>持有金额</dt>
                <dd class="mobile-holding-value">
                  <span>{{ formatMoney(row.holdingAmount || 0) }}</span>
                  <el-button :icon="Setting" circle size="small" @click.stop="openHoldingAction(row)" />
                </dd>
              </div>
              <div>
                <dt>成本净值</dt>
                <dd>{{ formatNumber(row.holdingCostNav) }}</dd>
              </div>
              <div>
                <dt>持有份额</dt>
                <dd>{{ formatShares(row.holdingShares) }}</dd>
              </div>
              <div>
                <dt>持有天数</dt>
                <dd>{{ row.firstBuyDate ? `${daysFromDate(row.firstBuyDate)}天` : '-' }}</dd>
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
          <el-descriptions-item label="成本净值">{{ formatNumber(selectedRow.holdingCostNav) }}</el-descriptions-item>
          <el-descriptions-item label="持有份额">{{ formatShares(selectedRow.holdingShares) }}</el-descriptions-item>
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

    <el-dialog v-model="groupDialogVisible" :title="editingGroup ? '重命名分组' : '新增分组'" width="420px" align-center>
      <el-form label-width="90px">
        <el-form-item label="分组名称">
          <el-input v-model="groupFormName" maxlength="30" show-word-limit placeholder="请输入分组名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingGroup" @click="saveGroup">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="groupManageVisible" title="管理分组" width="520px" align-center @open="initGroupEditor" @closed="resetGroupEditor">
      <div class="group-manage-list">
        <div
          v-for="group in visibleManagedGroups"
          :key="group.clientId"
          class="group-manage-row"
          :class="{ 'is-system-group': !group.editable, 'is-pending-delete': group.deleted }"
        >
          <div class="group-manage-main">
            <template v-if="group.editing && group.editable">
              <el-input
                v-model="group.name"
                maxlength="30"
                show-word-limit
                placeholder="请输入分组名称"
              />
            </template>
            <template v-else>
              <span>{{ group.name }}（{{ group.count }}）</span>
            </template>
          </div>
          <div>
            <template v-if="group.editable">
              <el-button
                link
                type="primary"
                @click="renameGroupRow(group)"
              >
                重命名
              </el-button>
              <el-button
                link
                type="danger"
                @click="deleteGroupRow(group)"
              >
                删除
              </el-button>
            </template>
            <template v-else>
              <el-tooltip content="系统字段不可删除" placement="top">
                <span class="disabled-group-action">
                  <el-button link type="primary" disabled>重命名</el-button>
                </span>
              </el-tooltip>
              <el-tooltip content="系统字段不可删除" placement="top">
                <span class="disabled-group-action">
                  <el-button link type="danger" disabled>删除</el-button>
                </span>
              </el-tooltip>
            </template>
          </div>
        </div>
        <button class="add-group-row" type="button" @click="addGroupEditRow">
          <el-icon><Plus /></el-icon>
        </button>
        <el-empty v-if="!visibleManagedGroups.length" description="暂无分组" />
      </div>
      <template #footer>
        <div class="group-manage-footer">
          <el-button type="primary" :loading="savingGroup" @click="saveGroupEdits">完成</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="batchGroupDialogVisible" title="选择分组" width="460px" align-center>
      <p class="batch-add-summary">已选择 {{ pendingAddFunds.length }} 只基金</p>
      <el-select
        v-model="batchGroupIds"
        multiple
        clearable
        collapse-tags
        collapse-tags-tooltip
        placeholder="选择分组（可选）"
        style="width: 100%"
      >
        <el-option label="全部" :value="-1" disabled />
        <el-option label="自选" :value="-2" disabled />
        <el-option
          v-for="group in customGroups"
          :key="group.id"
          :label="group.name"
          :value="group.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="batchGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="confirmBatchAddFunds">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="holdingActionVisible" title="持仓操作" width="520px" align-center>
      <template v-if="holdingActionRow">
        <div class="holding-action-head">
          <div>
            <strong>{{ holdingActionRow.name }}</strong>
            <span>{{ holdingActionRow.code }}</span>
          </div>
          <el-button plain @click="showTransactionRecords = !showTransactionRecords">交易记录</el-button>
        </div>

        <div v-if="showTransactionRecords" class="transaction-list">
          <div v-for="record in currentFundTransactions" :key="record.tradeTime + record.tradeType + record.amount" class="transaction-row">
            <div>
              <strong>{{ record.tradeType }}</strong>
              <span>{{ formatTradeTime(record.tradeTime) }}</span>
            </div>
            <b>{{ formatMoney(record.amount || 0) }}</b>
            <small>
              {{ formatMoney(record.beforeAmount || 0) }} → {{ formatMoney(record.afterAmount || 0) }}
              <template v-if="record.targetFundCode"> / {{ record.targetFundName }} {{ record.targetFundCode }}</template>
            </small>
          </div>
          <el-empty v-if="!currentFundTransactions.length" description="暂无交易记录" />
        </div>

        <div class="holding-action-grid">
          <el-button type="primary" plain @click="openTradeForm('加仓')">加仓</el-button>
          <el-button type="danger" plain @click="openTradeForm('减仓')">减仓</el-button>
          <el-button type="success" plain @click="openTradeForm('定投')">定投</el-button>
          <el-button type="warning" plain @click="openTradeForm('转换')">转换</el-button>
        </div>

        <div class="holding-action-secondary">
          <el-button plain @click="openTradeForm('编辑持仓')">编辑持仓</el-button>
          <el-button type="danger" plain @click="clearHolding">清空持仓</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="tradeFormVisible" :title="activeTradeType" width="520px" align-center>
      <el-form label-width="110px">
        <template v-if="activeTradeType === '编辑持仓'">
          <el-form-item label-width="0">
            <el-segmented v-model="holdingEditMode" :options="holdingEditModeOptions" />
          </el-form-item>
          <template v-if="holdingEditMode === 'amount'">
            <el-form-item label="持有金额" required>
              <el-input-number v-model="tradeAmount" :controls="false" :min="0" :precision="2" :step="1000" />
            </el-form-item>
            <el-form-item label="持有收益">
              <el-input-number v-model="tradeProfitAmount" :controls="false" :precision="2" :step="1000" />
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="持有份额" required>
              <el-input-number v-model="editHoldingShares" :controls="false" :min="0" :precision="4" :step="100" />
            </el-form-item>
            <el-form-item label="持仓成本价" required>
              <el-input-number v-model="tradeCostNav" :controls="false" :min="0" :precision="4" :step="0.01" />
            </el-form-item>
          </template>
          <el-form-item required>
            <template #label>
              <span>首次买入日期</span>
              <el-button class="date-mode-button" link type="primary" @click="toggleFirstBuyDateMode">
                {{ firstBuyDateMode === 'date' ? '按天数' : '按日期' }}
              </el-button>
            </template>
            <el-date-picker
              v-if="firstBuyDateMode === 'date'"
              v-model="firstBuyDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
            />
            <el-input-number
              v-else
              v-model="holdingDays"
              :controls="false"
              :min="0"
              :precision="0"
              :step="1"
              style="width: 100%"
            />
          </el-form-item>
        </template>
        <el-form-item v-else label="操作金额">
          <el-input-number v-model="tradeAmount" :min="0" :precision="2" :step="1000" />
        </el-form-item>
        <template v-if="activeTradeType === '转换'">
          <el-form-item label="目标基金">
            <el-select
              v-model="targetFundSelector"
              filterable
              clearable
              placeholder="选择已有基金"
              @change="selectExistingTargetFund"
            >
              <el-option
                v-for="item in watchlist.filter((fund) => fund.code !== holdingActionRow?.code)"
                :key="item.code"
                :label="`${item.code} ${item.name}`"
                :value="item.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="搜索基金">
            <el-select
              v-model="targetSearchCode"
              filterable
              remote
              clearable
              reserve-keyword
              placeholder="搜索基金代码或名称"
              :remote-method="remoteTargetSearch"
              :loading="targetSearching"
              @change="selectSearchedTargetFund"
            >
              <el-option
                v-for="item in targetSearchOptions"
                :key="item.code"
                :label="`${item.code} ${item.name}`"
                :value="item.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="手动输入">
            <div class="manual-target">
              <el-input v-model="manualTargetCode" placeholder="基金代码" />
              <el-input v-model="manualTargetName" placeholder="基金名称" />
            </div>
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input v-model="tradeRemark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tradeFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTradeForm">确认</el-button>
      </template>
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
  Plus,
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
  createFundGroup,
  deleteFundGroup,
  deleteWatchFund,
  estimateFund,
  listMarketIndices,
  listFundGroups,
  listFundTransactions,
  listWatchFunds,
  mergeCloudSnapshot,
  replaceCloudSnapshot,
  searchFunds,
  updateFundGroup,
  updateFundHolding,
  updateWatchFundGroups,
  type FundGroupId,
  type FundEstimateRow,
  type FundGroup,
  type MarketIndexRow,
  type FundSnapshot,
  type FundSearchItem,
  type FundTransaction
} from '../api/fund'

type PayMethod = 'alipay' | 'wechat'
type RefreshMode = 'manual' | 'standard' | 'fast'
type HoldingEditMode = 'amount' | 'shares'
type FirstBuyDateMode = 'date' | 'days'
type SortKey = 'default' | 'rate' | 'profit' | 'name'
type SortOrder = 'asc' | 'desc'
type TradeType = '加仓' | '减仓' | '定投' | '转换' | '编辑持仓' | '清空持仓' | ''
type GroupTab = {
  key: string
  id?: FundGroupId
  name: string
  count: number
  type: 'all' | 'watch' | 'custom'
}
type GroupEditRow = {
  clientId: string
  id?: FundGroupId
  name: string
  originalName: string
  count: number
  editable: boolean
  groupType?: string
  isNew: boolean
  deleted: boolean
  editing: boolean
}

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
const holdingEditModeOptions = [
  { label: '按金额', value: 'amount' },
  { label: '按份额', value: 'shares' }
]
const sortOptions = [
  { label: '默认', value: 'default' },
  { label: '涨跌幅', value: 'rate' },
  { label: '持有收益', value: 'profit' },
  { label: '名称', value: 'name' }
]
const localSnapshotKey = 'YJH_LOCAL_FUND_SNAPSHOT'

const router = useRouter()
const activeSection = ref('overview')
const selectedCodes = ref<string[]>([])
const searching = ref(false)
const refreshing = ref(false)
const indicesLoading = ref(false)
const initialLoading = ref(true)
const adding = ref(false)
const unauthorized = ref(false)
const loadError = ref('')
const authenticated = ref(hasToken())
const coffeeDialogVisible = ref(false)
const settingsVisible = ref(false)
const trendVisible = ref(false)
const groupDialogVisible = ref(false)
const groupManageVisible = ref(false)
const batchGroupDialogVisible = ref(false)
const holdingActionVisible = ref(false)
const tradeFormVisible = ref(false)
const showTransactionRecords = ref(false)
const documentVisible = ref(document.visibilityState === 'visible')
const refreshMode = ref<RefreshMode>('standard')
const payMethod = ref<PayMethod>('alipay')
const searchOptions = ref<FundSearchItem[]>([])
const watchlist = ref<FundEstimateRow[]>([])
const marketIndices = ref<MarketIndexRow[]>([])
const systemGroups = ref<FundGroup[]>([])
const customGroups = ref<FundGroup[]>([])
const groupEditRows = ref<GroupEditRow[]>([])
const fundTransactions = ref<FundTransaction[]>([])
const pendingAddFunds = ref<FundSearchItem[]>([])
const activeGroupKey = ref('all')
const sortKey = ref<SortKey>('default')
const sortOrder = ref<SortOrder>('desc')
const groupFormName = ref('')
const savingGroup = ref(false)
const editingGroup = ref<FundGroup | null>(null)
const lastUpdated = ref('')
const batchGroupIds = ref<FundGroupId[]>([])
const userInfo = ref<UserInfo | null>(null)
const selectedRow = ref<FundEstimateRow | null>(null)
const holdingActionRow = ref<FundEstimateRow | null>(null)
const activeTradeType = ref<TradeType>('')
const holdingEditMode = ref<HoldingEditMode>('amount')
const firstBuyDateMode = ref<FirstBuyDateMode>('date')
const tradeAmount = ref(0)
const tradeProfitAmount = ref(0)
const tradeCostNav = ref(0)
const editHoldingShares = ref(0)
const firstBuyDate = ref('')
const holdingDays = ref(0)
const tradeRemark = ref('')
const targetFundSelector = ref('')
const targetSearchCode = ref('')
const targetSearching = ref(false)
const targetSearchOptions = ref<FundSearchItem[]>([])
const manualTargetCode = ref('')
const manualTargetName = ref('')
let searchTimer: number | undefined
let targetSearchTimer: number | undefined
let refreshTimer: number | undefined
let controller: AbortController | null = null
let syncingLocalAndCloud = false
let mirrorNextCloudSnapshot = false

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
const totalPrincipal = computed(() => watchlist.value.reduce((sum, item) => sum + Number(item.holdingCost || 0), 0))
const portfolioChange = computed(() => totalPrincipal.value ? totalProfit.value / totalPrincipal.value * 100 : 0)
const groupTabs = computed<GroupTab[]>(() => [
  { key: 'all', name: '全部', count: new Set(watchlist.value.map((item) => item.code)).size, type: 'all' },
  { key: 'watch', name: '自选', count: watchlist.value.length, type: 'watch' },
  ...customGroups.value.map((group) => ({
    key: `custom-${group.id}`,
    id: group.id,
    name: group.name,
    count: countFundsByGroup(group.id),
    type: 'custom' as const
  }))
])
const managedGroups = computed<FundGroup[]>(() => [
  ...systemManageGroups.value,
  ...customGroups.value.map((group) => ({ ...group, count: countFundsByGroup(group.id) }))
])
const visibleManagedGroups = computed<GroupEditRow[]>(() => groupEditRows.value.filter((group) => !group.deleted))
const systemManageGroups = computed<FundGroup[]>(() => {
  const allCount = new Set(watchlist.value.map((item) => item.code)).size
  const watchCount = watchlist.value.length
  const allGroup = systemGroups.value.find((group) => group.name === '全部')
  const watchGroup = systemGroups.value.find((group) => group.name === '自选')
  return [
    { id: allGroup?.id ?? -1, name: '全部', count: allCount, groupType: 'SYSTEM', editable: false },
    { id: watchGroup?.id ?? -2, name: '自选', count: watchCount, groupType: 'SYSTEM', editable: false }
  ]
})
const activeGroup = computed(() => groupTabs.value.find((group) => group.key === activeGroupKey.value) || groupTabs.value[0])
const filteredWatchlist = computed(() => {
  const group = activeGroup.value
  if (!group || group.type === 'all' || group.type === 'watch') {
    return watchlist.value
  }
  return watchlist.value.filter((item) => (item.groupIds || []).includes(group.id || 0))
})
const sortedWatchlist = computed(() => {
  const items = [...filteredWatchlist.value]
  if (sortKey.value === 'default') {
    return items
  }
  const direction = sortOrder.value === 'asc' ? 1 : -1
  return items.sort((left, right) => {
    if (sortKey.value === 'name') {
      return left.name.localeCompare(right.name, 'zh-CN') * direction
    }
    const leftValue = sortKey.value === 'rate'
      ? Number(left.estimateRate || 0)
      : Number(left.estimateProfit || 0)
    const rightValue = sortKey.value === 'rate'
      ? Number(right.estimateRate || 0)
      : Number(right.estimateProfit || 0)
    return (leftValue - rightValue) * direction
  })
})
const emptyDescription = computed(() => {
  if (watchlist.value.length === 0) {
    return '暂无自选基金，搜索基金后添加到工作台。'
  }
  return '当前分组暂无基金。'
})
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
  { label: '持有金额', value: formatMoney(totalHolding.value), hint: '当前持仓市值口径', tone: '' },
  { label: '组合涨跌', value: formatPercent(portfolioChange.value), hint: '预估盈亏 / 投入本金', tone: toneClass(portfolioChange.value) },
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
const currentFundTransactions = computed(() => {
  if (!holdingActionRow.value) {
    return []
  }
  return fundTransactions.value
    .filter((record) => record.fundCode === holdingActionRow.value?.code)
    .sort((a, b) => Date.parse(b.tradeTime) - Date.parse(a.tradeTime))
})
onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  void loadUserProfile()
  void loadMarketIndices()
  void loadGroups()
  void loadWatchlist(true)
})

onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.clearTimeout(searchTimer)
  window.clearTimeout(targetSearchTimer)
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

function isCustomGroup(group: FundGroup) {
  return group.editable !== false && group.groupType !== 'SYSTEM'
}

function isGroupEditable(group: FundGroup) {
  return isCustomGroup(group)
}

function toGroupEditRow(group: FundGroup): GroupEditRow {
  const editable = isGroupEditable(group)
  return {
    clientId: `${editable ? 'custom' : 'system'}-${group.id}`,
    id: group.id,
    name: group.name,
    originalName: group.name,
    count: group.count || 0,
    editable,
    groupType: group.groupType,
    isNew: false,
    deleted: false,
    editing: false
  }
}

function initGroupEditor() {
  groupEditRows.value = managedGroups.value.map(toGroupEditRow)
}

function resetGroupEditor() {
  groupEditRows.value = []
}

function addGroupEditRow() {
  groupEditRows.value.push({
    clientId: `new-${Date.now()}-${Math.random()}`,
    name: '',
    originalName: '',
    count: 0,
    editable: true,
    groupType: 'CUSTOM',
    isNew: true,
    deleted: false,
    editing: true
  })
}

function renameGroupRow(group: GroupEditRow) {
  group.editing = true
}

async function deleteGroupRow(group: GroupEditRow) {
  if (group.isNew) {
    groupEditRows.value = groupEditRows.value.filter((item) => item.clientId !== group.clientId)
    return
  }
  if (!group.id) {
    return
  }
  try {
    if (hasToken()) {
      await deleteFundGroup(group.id)
      mirrorNextCloudSnapshot = true
    } else {
      deleteLocalGroup(group.id)
    }
    groupEditRows.value = groupEditRows.value.filter((item) => item.clientId !== group.clientId)
    if (activeGroupKey.value === `custom-${group.id}`) {
      activeGroupKey.value = 'all'
    }
    await loadGroups()
    await loadWatchlist(true)
    initGroupEditor()
    ElMessage.success('分组已删除')
  } catch (error) {
    handleError(error, '删除分组失败')
    await loadGroups()
    await loadWatchlist(true)
    initGroupEditor()
  }
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

async function loadGroups() {
  if (!hasToken()) {
    systemGroups.value = []
    customGroups.value = readLocalSnapshot().groups.map((group) => ({ ...group, count: 0 }))
    return
  }
  try {
    const groups = await listFundGroups()
    systemGroups.value = groups.filter((group) => !isCustomGroup(group))
    customGroups.value = groups.filter(isCustomGroup)
    if (!groupTabs.value.some((group) => group.key === activeGroupKey.value)) {
      activeGroupKey.value = 'all'
    }
  } catch (error) {
    if (isUnauthorized(error)) {
      systemGroups.value = []
      customGroups.value = []
      return
    }
    handleError(error, '加载基金分组失败')
  }
}

async function loadMarketIndices() {
  indicesLoading.value = true
  try {
    marketIndices.value = await listMarketIndices()
  } catch {
    marketIndices.value = []
  } finally {
    indicesLoading.value = false
  }
}

async function loadTransactions() {
  if (!hasToken()) {
    fundTransactions.value = readLocalSnapshot().transactions
    return
  }
  try {
    fundTransactions.value = await listFundTransactions()
  } catch (error) {
    if (isUnauthorized(error)) {
      fundTransactions.value = []
      return
    }
    handleError(error, '加载交易记录失败')
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
  const options = selectedCodes.value
    .map((code) => searchOptions.value.find((item) => item.code === code))
    .filter((item): item is FundSearchItem => Boolean(item))
  if (!options.length) {
    ElMessage.warning('请先选择基金')
    return
  }
  pendingAddFunds.value = options
  batchGroupIds.value = [-1, -2]
  batchGroupDialogVisible.value = true
}

async function confirmBatchAddFunds() {
  if (!pendingAddFunds.value.length) {
    ElMessage.warning('请先选择基金')
    return
  }
  adding.value = true
  try {
    const groupIds = batchGroupIds.value.filter((id) => id !== -1 && id !== -2)
    if (hasToken()) {
      for (const option of pendingAddFunds.value) {
        await addWatchFund(option.code, option.name, 0)
        await updateWatchFundGroups(option.code, groupIds)
      }
      mirrorNextCloudSnapshot = true
    } else {
      pendingAddFunds.value.forEach((option, index) => {
        upsertLocalFund({
          id: Date.now() + index,
          code: option.code,
          name: option.name,
          holdingAmount: 0,
          holdingCost: 0,
          holdingCostNav: 0,
          holdingShares: 0,
          firstBuyDate: '',
          groupIds: [...groupIds]
        })
      })
    }
    selectedCodes.value = []
    searchOptions.value = []
    pendingAddFunds.value = []
    batchGroupIds.value = [-1, -2]
    batchGroupDialogVisible.value = false
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
  if (manual) {
    void loadMarketIndices()
  }
  authenticated.value = hasToken()
  loadError.value = ''
  if (!authenticated.value) {
    const snapshot = readLocalSnapshot()
    applySnapshotToPage(await hydrateLocalSnapshot(snapshot))
    unauthorized.value = false
    refreshing.value = false
    initialLoading.value = false
    lastUpdated.value = snapshotHasData(snapshot) ? '本地缓存' : ''
    return
  }
  try {
    const cloudSnapshot = await fetchCloudSnapshot(controller.signal)
    const displaySnapshot = await reconcileLocalAndCloud(cloudSnapshot)
    applySnapshotToPage(displaySnapshot)
    unauthorized.value = false
    lastUpdated.value = watchlist.value.find((item) => item.estimateTime)?.estimateTime
      || new Date().toLocaleString('zh-CN', { hour12: false })
  } catch (error) {
    if (isAbortError(error)) {
      return
    }
    if (isUnauthorized(error)) {
      unauthorized.value = true
      clearAuthStorage()
      authenticated.value = false
      applySnapshotToPage(readLocalSnapshot())
      unauthorized.value = false
      lastUpdated.value = watchlist.value.length ? '本地缓存' : ''
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

async function saveFundGroups(row: FundEstimateRow) {
  try {
    row.groupIds = row.groupIds || []
    if (hasToken()) {
      await updateWatchFundGroups(row.code, row.groupIds)
      mirrorNextCloudSnapshot = true
      await loadGroups()
      persistCurrentSnapshot()
    } else {
      persistCurrentSnapshot()
    }
    ElMessage.success('基金分组已保存')
  } catch (error) {
    handleError(error, '保存基金分组失败')
    await loadWatchlist(true)
  }
}

function openCreateGroup() {
  editingGroup.value = null
  groupFormName.value = ''
  groupDialogVisible.value = true
}

function openEditGroup(group: FundGroup) {
  editingGroup.value = group
  groupFormName.value = group.name
  groupDialogVisible.value = true
}

async function saveGroup() {
  const name = groupFormName.value.trim()
  if (!name) {
    ElMessage.warning('请输入分组名称')
    return
  }
  savingGroup.value = true
  try {
    if (editingGroup.value) {
      if (hasToken()) {
        await updateFundGroup(editingGroup.value.id, name)
        mirrorNextCloudSnapshot = true
      } else {
        updateLocalGroup(editingGroup.value.id, name)
      }
      ElMessage.success('分组已重命名')
    } else {
      const group = hasToken()
        ? await createFundGroup(name)
        : createLocalGroup(name)
      if (hasToken()) {
        mirrorNextCloudSnapshot = true
      }
      activeGroupKey.value = `custom-${group.id}`
      ElMessage.success('分组已新增')
    }
    groupDialogVisible.value = false
    await loadGroups()
    if (hasToken()) {
      persistCurrentSnapshot()
    }
  } catch (error) {
    handleError(error, '保存分组失败')
  } finally {
    savingGroup.value = false
  }
}

async function saveGroupEdits() {
  const rows = groupEditRows.value.filter((group) => group.editable)
  const activeRows = rows.filter((group) => !group.deleted)
  activeRows.forEach((group) => {
    group.name = group.name.trim()
  })
  const names = activeRows.map((group) => group.name)
  const emptyRow = activeRows.find((group) => !group.name)
  if (emptyRow) {
    emptyRow.editing = true
    ElMessage.warning('请输入分组名称')
    return
  }
  if (names.some((name) => name === '全部' || name === '自选')) {
    ElMessage.warning('系统分组不能修改')
    return
  }
  if (new Set(names).size !== names.length) {
    ElMessage.warning('基金分组名称已存在')
    return
  }

  savingGroup.value = true
  try {
    const updatedRows = activeRows.filter((group) => !group.isNew && group.id && group.name.trim() !== group.originalName)
    const createdRows = activeRows.filter((group) => group.isNew)

    for (const group of updatedRows) {
      const name = group.name.trim()
      if (!group.id) {
        continue
      }
      if (hasToken()) {
        await updateFundGroup(group.id, name)
        mirrorNextCloudSnapshot = true
      } else {
        updateLocalGroup(group.id, name)
      }
    }

    let lastCreatedGroup: FundGroup | null = null
    for (const group of createdRows) {
      const name = group.name.trim()
      lastCreatedGroup = hasToken()
        ? await createFundGroup(name)
        : createLocalGroup(name)
      if (hasToken()) {
        mirrorNextCloudSnapshot = true
      }
    }

    if (lastCreatedGroup) {
      activeGroupKey.value = `custom-${lastCreatedGroup.id}`
    }
    groupEditRows.value = []
    await loadGroups()
    await loadWatchlist(true)
    if (hasToken()) {
      persistCurrentSnapshot()
    }
    ElMessage.success('分组已保存')
    groupManageVisible.value = false
  } catch (error) {
    handleError(error, '保存分组失败')
  } finally {
    savingGroup.value = false
  }
}

async function removeGroup(group: FundGroup) {
  try {
    await ElMessageBox.confirm(`确定删除分组“${group.name}”吗？该操作不会删除基金。`, '删除分组', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    if (hasToken()) {
      await deleteFundGroup(group.id)
      mirrorNextCloudSnapshot = true
    } else {
      deleteLocalGroup(group.id)
    }
    if (activeGroupKey.value === `custom-${group.id}`) {
      activeGroupKey.value = 'all'
    }
    ElMessage.success('分组已删除')
    await loadGroups()
    await loadWatchlist(true)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    handleError(error, '删除分组失败')
  }
}

async function saveHolding(row: FundEstimateRow) {
  try {
    const beforeAmount = Number(row.holdingAmount || 0)
    if (hasToken()) {
      await updateFundHolding(row.code, row.holdingAmount || 0, row.holdingCost || 0, row.holdingCostNav || 0, row.holdingShares || 0, row.firstBuyDate || '')
      mirrorNextCloudSnapshot = true
    } else {
      persistCurrentSnapshot()
    }
    addTransactionRecord({
      fundCode: row.code,
      fundName: row.name,
      tradeType: '编辑持仓',
      amount: Math.abs(Number(row.holdingAmount || 0) - beforeAmount),
      beforeAmount,
      afterAmount: Number(row.holdingAmount || 0),
      tradeTime: new Date().toISOString()
    })
    ElMessage.success('持有金额已保存')
    await syncSnapshotAfterLocalChange()
  } catch (error) {
    handleError(error, '保存持有金额失败')
  }
}

function openHoldingAction(row: FundEstimateRow) {
  holdingActionRow.value = row
  showTransactionRecords.value = false
  holdingActionVisible.value = true
}

function openTradeForm(type: TradeType) {
  activeTradeType.value = type
  tradeAmount.value = type === '编辑持仓' ? Number(holdingActionRow.value?.holdingAmount || 0) : 0
  tradeProfitAmount.value = type === '编辑持仓' ? Number(holdingActionRow.value?.estimateProfit || 0) : 0
  const principal = Number(holdingActionRow.value?.holdingAmount || 0)
  const shares = Number(holdingActionRow.value?.holdingShares || 0)
  holdingEditMode.value = 'amount'
  firstBuyDateMode.value = 'date'
  tradeCostNav.value = type === '编辑持仓'
    ? Number(holdingActionRow.value?.holdingCostNav || (principal > 0 && shares > 0 ? principal / shares : 0))
    : 0
  editHoldingShares.value = type === '编辑持仓' ? Number(holdingActionRow.value?.holdingShares || 0) : 0
  firstBuyDate.value = type === '编辑持仓' ? String(holdingActionRow.value?.firstBuyDate || '') : ''
  holdingDays.value = firstBuyDate.value ? daysFromDate(firstBuyDate.value) : 0
  tradeRemark.value = ''
  targetFundSelector.value = ''
  targetSearchCode.value = ''
  targetSearchOptions.value = []
  manualTargetCode.value = ''
  manualTargetName.value = ''
  tradeFormVisible.value = true
}

function latestNavOf(row: FundEstimateRow | null) {
  return Number(row?.estimateNav || row?.previousNav || 0)
}

async function ensureRealtimeEstimate(row: FundEstimateRow) {
  if (latestNavOf(row) > 0) {
    return
  }
  const estimate = await estimateFund(row.code)
  row.name = estimate.name || row.name
  row.navDate = estimate.navDate
  row.previousNav = estimate.previousNav
  row.estimateNav = estimate.estimateNav
  row.estimateRate = estimate.estimateRate
  row.estimateTime = estimate.estimateTime
  row.error = estimate.error
}

function daysFromDate(value: string) {
  const parsed = Date.parse(`${value}T00:00:00`)
  if (Number.isNaN(parsed)) {
    return 0
  }
  return Math.max(0, Math.floor((Date.now() - parsed) / 86400000))
}

function dateFromDays(days: number) {
  const date = new Date()
  date.setDate(date.getDate() - Math.max(0, Number(days || 0)))
  return formatDateInput(date)
}

function formatDateInput(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function toggleFirstBuyDateMode() {
  if (firstBuyDateMode.value === 'date') {
    holdingDays.value = firstBuyDate.value ? daysFromDate(firstBuyDate.value) : 0
    firstBuyDateMode.value = 'days'
  } else {
    firstBuyDate.value = dateFromDays(holdingDays.value)
    firstBuyDateMode.value = 'date'
  }
}

function toggleSortOrder() {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
}

function resolveFirstBuyDate() {
  return firstBuyDateMode.value === 'date'
    ? firstBuyDate.value
    : dateFromDays(holdingDays.value)
}

function remoteTargetSearch(keyword: string) {
  window.clearTimeout(targetSearchTimer)
  targetSearchTimer = window.setTimeout(async () => {
    if (!keyword.trim()) {
      targetSearchOptions.value = []
      return
    }
    targetSearching.value = true
    try {
      targetSearchOptions.value = await searchFunds(keyword)
    } catch (error) {
      handleError(error, '基金搜索失败')
    } finally {
      targetSearching.value = false
    }
  }, 260)
}

function selectExistingTargetFund(code: string) {
  const target = watchlist.value.find((fund) => fund.code === code)
  if (target) {
    manualTargetCode.value = target.code
    manualTargetName.value = target.name
  }
}

function selectSearchedTargetFund(code: string) {
  const target = targetSearchOptions.value.find((fund) => fund.code === code)
  if (target) {
    manualTargetCode.value = target.code
    manualTargetName.value = target.name
  }
}

async function submitTradeForm() {
  const row = holdingActionRow.value
  if (!row || !activeTradeType.value) {
    return
  }
  const amount = Number(tradeAmount.value || 0)
  if (amount <= 0 && activeTradeType.value !== '编辑持仓') {
    ElMessage.warning('请输入大于 0 的金额')
    return
  }
  try {
    if (activeTradeType.value === '转换') {
      await applyTransfer(row, amount)
    } else {
      await applyHoldingTrade(row, activeTradeType.value, amount)
    }
    tradeFormVisible.value = false
    ElMessage.success('持仓操作已完成')
  } catch (error) {
    handleError(error, '持仓操作失败')
  }
}

async function applyHoldingTrade(row: FundEstimateRow, type: TradeType, amount: number) {
  const beforeAmount = Number(row.holdingAmount || 0)
  let afterAmount = beforeAmount
  if (type === '加仓' || type === '定投') {
    afterAmount = beforeAmount + amount
  } else if (type === '减仓') {
    afterAmount = beforeAmount - amount
    if (afterAmount < 0) {
      throw new Error('减仓金额不能大于当前持仓')
    }
  } else if (type === '编辑持仓') {
    afterAmount = amount
  }
  if (type === '编辑持仓') {
    const buyDate = resolveFirstBuyDate()
    if (!buyDate) {
      throw new Error('请选择首次买入日期')
    }
    await ensureRealtimeEstimate(row)
    const latestNav = latestNavOf(row)
    if (holdingEditMode.value === 'amount') {
      const marketValue = Number(tradeAmount.value || 0)
      const profit = Number(tradeProfitAmount.value || 0)
      const principal = marketValue - profit
      if (marketValue <= 0) {
        throw new Error('请输入有效的持有金额')
      }
      if (principal <= 0) {
        throw new Error('持有收益不能大于或等于持有金额')
      }
      if (latestNav <= 0) {
        throw new Error('当前基金净值缺失，无法按金额计算份额')
      }
      row.holdingAmount = marketValue
      row.holdingShares = marketValue / latestNav
      row.holdingCostNav = principal / row.holdingShares
      row.holdingCost = principal
      row.firstBuyDate = buyDate
    } else {
      const shares = Number(editHoldingShares.value || 0)
      const costNav = Number(tradeCostNav.value || 0)
      if (shares <= 0 || costNav <= 0) {
        throw new Error('请输入有效的持有份额和持仓成本价')
      }
      row.holdingShares = shares
      row.holdingCostNav = costNav
      row.holdingCost = shares * costNav
      row.holdingAmount = latestNav > 0 ? shares * latestNav : row.holdingCost
      row.firstBuyDate = buyDate
    }
    afterAmount = row.holdingAmount
  } else {
    row.holdingAmount = afterAmount
  }
  recalculateEstimate(row)
  if (hasToken()) {
    await updateFundHolding(row.code, row.holdingAmount || 0, row.holdingCost || 0, row.holdingCostNav || 0, row.holdingShares || 0, row.firstBuyDate || '')
    mirrorNextCloudSnapshot = true
  }
  addTransactionRecord({
    fundCode: row.code,
    fundName: row.name,
    tradeType: type,
    amount: type === '编辑持仓' ? Math.abs(afterAmount - beforeAmount) : amount,
    beforeAmount,
    afterAmount,
    remark: tradeRemark.value.trim(),
    tradeTime: new Date().toISOString()
  })
  await syncSnapshotAfterLocalChange()
}

async function applyTransfer(row: FundEstimateRow, amount: number) {
  const targetCode = manualTargetCode.value.trim()
  const targetName = manualTargetName.value.trim() || targetCode
  if (!targetCode && !targetName) {
    throw new Error('请选择或输入目标基金')
  }
  const beforeAmount = Number(row.holdingAmount || 0)
  const afterAmount = beforeAmount - amount
  if (afterAmount < 0) {
    throw new Error('转换金额不能大于当前持仓')
  }
  row.holdingAmount = afterAmount
  recalculateEstimate(row)
  let target = watchlist.value.find((fund) => fund.code === targetCode)
  const targetBefore = Number(target?.holdingAmount || 0)
  if (!target) {
    target = {
      id: Date.now(),
      code: targetCode || targetName,
      name: targetName,
      holdingAmount: 0,
      holdingCost: 0,
      holdingCostNav: 0,
      holdingShares: 0,
      firstBuyDate: '',
      groupIds: []
    }
    watchlist.value.unshift(target)
  }
  target.holdingAmount = targetBefore + amount
  recalculateEstimate(target)
  addTransactionRecord({
    fundCode: row.code,
    fundName: row.name,
    tradeType: '转换',
    amount,
    beforeAmount,
    afterAmount,
    targetFundCode: target.code,
    targetFundName: target.name,
    remark: tradeRemark.value.trim(),
    tradeTime: new Date().toISOString()
  })
  addTransactionRecord({
    fundCode: target.code,
    fundName: target.name,
    tradeType: '转换转入',
    amount,
    beforeAmount: targetBefore,
    afterAmount: target.holdingAmount,
    targetFundCode: row.code,
    targetFundName: row.name,
    remark: tradeRemark.value.trim(),
    tradeTime: new Date().toISOString()
  })
  await syncSnapshotAfterLocalChange()
}

async function clearHolding() {
  const row = holdingActionRow.value
  if (!row) {
    return
  }
  try {
    await ElMessageBox.confirm('确定清空该基金持仓吗？', '清空持仓', {
      type: 'warning',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
    const beforeAmount = Number(row.holdingAmount || 0)
    row.holdingAmount = 0
    row.holdingCost = 0
    row.holdingCostNav = 0
    row.holdingShares = 0
    row.firstBuyDate = ''
    addTransactionRecord({
      fundCode: row.code,
      fundName: row.name,
      tradeType: '清空持仓',
      amount: beforeAmount,
      beforeAmount,
      afterAmount: 0,
      tradeTime: new Date().toISOString()
    })
    await syncSnapshotAfterLocalChange()
    ElMessage.success('持仓已清空')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    handleError(error, '清空持仓失败')
  }
}

function addTransactionRecord(record: FundTransaction) {
  fundTransactions.value.unshift(record)
  persistCurrentSnapshot()
}

function recalculateEstimate(row: FundEstimateRow) {
  const currentMarketValue = Number(row.holdingAmount || 0)
  const principal = Number(row.holdingCost || 0) || currentMarketValue
  const nav = latestNavOf(row)
  let shares = Number(row.holdingShares || 0)
  if (shares <= 0 && Number(row.holdingCostNav || 0) > 0) {
    shares = principal / Number(row.holdingCostNav || 0)
    row.holdingShares = shares
  }
  if (shares > 0 && nav > 0) {
    row.estimateMarketValue = shares * nav
    row.estimateProfit = row.estimateMarketValue - principal
    row.holdingAmount = row.estimateMarketValue
  } else {
    row.estimateMarketValue = currentMarketValue
    row.estimateProfit = currentMarketValue - principal
  }
}

async function syncSnapshotAfterLocalChange() {
  persistCurrentSnapshot()
  if (hasToken()) {
    await replaceCloudSnapshot(readLocalSnapshot())
    clearLocalSnapshot()
    mirrorNextCloudSnapshot = true
  }
}

async function removeFund(code: string) {
  try {
    await ElMessageBox.confirm('确定删除这只自选基金吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    if (hasToken()) {
      await deleteWatchFund(code)
      mirrorNextCloudSnapshot = true
    } else {
      deleteLocalFund(code)
    }
    ElMessage.success('已删除自选基金')
    await loadWatchlist(true)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    handleError(error, '删除基金失败')
  }
}

async function fetchCloudSnapshot(signal?: AbortSignal): Promise<FundSnapshot> {
  const [funds, groups, transactions] = await Promise.all([
    listWatchFunds(signal),
    listFundGroups(),
    listFundTransactions()
  ])
  const customOnlyGroups = groups.filter(isCustomGroup)
  return {
    funds: funds.map((fund) => ({
      code: fund.code,
      name: fund.name,
      holdingAmount: Number(fund.holdingAmount || 0),
      holdingCost: Number(fund.holdingCost || 0),
      holdingCostNav: Number(fund.holdingCostNav || 0),
      holdingShares: Number(fund.holdingShares || 0),
      firstBuyDate: fund.firstBuyDate || '',
      navDate: fund.navDate,
      previousNav: fund.previousNav,
      estimateNav: fund.estimateNav,
      estimateRate: fund.estimateRate,
      estimateProfit: fund.estimateProfit,
      estimateMarketValue: fund.estimateMarketValue,
      estimateTime: fund.estimateTime,
      error: fund.error,
      groupIds: fund.groupIds || []
    })),
    groups: customOnlyGroups.map((group) => ({
      id: group.id,
      name: group.name
    })),
    transactions
  }
}

async function reconcileLocalAndCloud(cloudSnapshot: FundSnapshot): Promise<FundSnapshot> {
  const localSnapshot = readLocalSnapshot()
  const localHasData = snapshotHasData(localSnapshot)
  const cloudHasData = snapshotHasData(cloudSnapshot)
  if (mirrorNextCloudSnapshot) {
    mirrorNextCloudSnapshot = false
    clearLocalSnapshot()
    return cloudSnapshot
  }
  if (syncingLocalAndCloud || snapshotsEqual(localSnapshot, cloudSnapshot)) {
    clearLocalSnapshot()
    return cloudSnapshot
  }
  if (!localHasData) {
    clearLocalSnapshot()
    return cloudSnapshot
  }
  if (!cloudHasData) {
    syncingLocalAndCloud = true
    try {
      await replaceCloudSnapshot(localSnapshot)
      ElMessage.success('本地数据已同步到云端')
      const refreshed = await fetchCloudSnapshot()
      clearLocalSnapshot()
      return refreshed
    } finally {
      syncingLocalAndCloud = false
    }
  }

  const useLocal = await askLocalOrCloud()
  if (!useLocal) {
    clearLocalSnapshot()
    ElMessage.success('已使用云端数据覆盖本地')
    return cloudSnapshot
  }
  const merge = await askLocalSyncMode()
  syncingLocalAndCloud = true
  try {
    if (merge) {
      await mergeCloudSnapshot(localSnapshot)
      ElMessage.success('本地数据已追加到云端')
    } else {
      await replaceCloudSnapshot(localSnapshot)
      ElMessage.success('本地数据已替换云端')
    }
    const refreshed = await fetchCloudSnapshot()
    clearLocalSnapshot()
    return refreshed
  } finally {
    syncingLocalAndCloud = false
  }
}

async function askLocalOrCloud() {
  try {
    await ElMessageBox.confirm('检测到当前浏览器本地数据和登录用户云端数据都存在，请选择使用哪一份数据。', '数据同步', {
      confirmButtonText: '本地覆盖云端',
      cancelButtonText: '云端覆盖本地',
      distinguishCancelAndClose: true,
      closeOnClickModal: false,
      closeOnPressEscape: false,
      showClose: false,
      type: 'warning'
    })
    return true
  } catch (action) {
    return action !== 'close' ? false : false
  }
}

async function askLocalSyncMode() {
  try {
    await ElMessageBox.confirm('请选择本地数据同步到云端的方式。', '本地覆盖云端', {
      confirmButtonText: '追加数据到云端',
      cancelButtonText: '替换云端数据',
      distinguishCancelAndClose: true,
      closeOnClickModal: false,
      closeOnPressEscape: false,
      showClose: false,
      type: 'warning'
    })
    return true
  } catch (action) {
    return action === 'cancel' ? false : false
  }
}

function applySnapshotToPage(snapshot: FundSnapshot) {
  customGroups.value = snapshot.groups.map((group) => ({ ...group, count: 0 }))
  fundTransactions.value = snapshot.transactions || []
  watchlist.value = snapshot.funds.map((fund, index) => ({
    id: index + 1,
    code: fund.code,
    name: fund.name,
    holdingAmount: Number(fund.holdingAmount || 0),
    holdingCost: Number(fund.holdingCost || 0),
    holdingCostNav: Number(fund.holdingCostNav || 0),
    holdingShares: Number(fund.holdingShares || 0),
    firstBuyDate: fund.firstBuyDate || '',
    navDate: fund.navDate,
    previousNav: fund.previousNav,
    estimateNav: fund.estimateNav,
    estimateRate: fund.estimateRate,
    estimateProfit: fund.estimateProfit,
    estimateMarketValue: fund.estimateMarketValue,
    estimateTime: fund.estimateTime,
    error: fund.error,
    groupIds: fund.groupIds || []
  }))
  if (!groupTabs.value.some((group) => group.key === activeGroupKey.value)) {
    activeGroupKey.value = 'all'
  }
}

async function hydrateLocalSnapshot(snapshot: FundSnapshot): Promise<FundSnapshot> {
  const funds = await Promise.all(snapshot.funds.map(async (fund) => {
    try {
      const estimate = await estimateFund(fund.code)
      const nav = Number(estimate.estimateNav || estimate.previousNav || 0)
      let shares = Number(fund.holdingShares || 0)
      if (shares <= 0 && Number(fund.holdingCostNav || 0) > 0) {
        shares = Number(fund.holdingAmount || 0) / Number(fund.holdingCostNav || 0)
      }
      const marketValue = shares > 0 && nav > 0 ? shares * nav : Number(fund.holdingAmount || 0)
      return {
        ...fund,
        name: estimate.name || fund.name,
        navDate: estimate.navDate,
        previousNav: estimate.previousNav,
        estimateNav: estimate.estimateNav,
        estimateRate: estimate.estimateRate,
        holdingShares: shares,
        firstBuyDate: fund.firstBuyDate || '',
        estimateProfit: marketValue - Number(fund.holdingAmount || 0),
        estimateMarketValue: marketValue,
        estimateTime: estimate.estimateTime,
        error: estimate.error
      }
    } catch {
      return fund
    }
  }))
  return { ...snapshot, funds }
}

function readLocalSnapshot(): FundSnapshot {
  if (typeof window === 'undefined') {
    return emptySnapshot()
  }
  try {
    const parsed = JSON.parse(localStorage.getItem(localSnapshotKey) || '')
    return normalizeSnapshot(parsed)
  } catch {
    return emptySnapshot()
  }
}

function saveLocalSnapshot(snapshot: FundSnapshot) {
  localStorage.setItem(localSnapshotKey, JSON.stringify(normalizeSnapshot(snapshot)))
}

function clearLocalSnapshot() {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(localSnapshotKey)
  }
}

function persistCurrentSnapshot() {
  saveLocalSnapshot({
    funds: watchlist.value.map((fund) => ({
      code: fund.code,
      name: fund.name,
      holdingAmount: Number(fund.holdingAmount || 0),
      holdingCost: Number(fund.holdingCost || 0),
      holdingCostNav: Number(fund.holdingCostNav || 0),
      holdingShares: Number(fund.holdingShares || 0),
      firstBuyDate: fund.firstBuyDate || '',
      groupIds: fund.groupIds || []
    })),
    groups: customGroups.value.map((group) => ({
      id: group.id,
      name: group.name
    })),
    transactions: fundTransactions.value
  })
}

function upsertLocalFund(fund: FundEstimateRow) {
  const index = watchlist.value.findIndex((item) => item.code === fund.code)
  if (index >= 0) {
    watchlist.value[index] = {
      ...watchlist.value[index],
      name: fund.name,
      holdingAmount: fund.holdingAmount,
      holdingCost: fund.holdingCost,
      holdingCostNav: fund.holdingCostNav,
      holdingShares: fund.holdingShares,
      firstBuyDate: fund.firstBuyDate,
      groupIds: fund.groupIds || watchlist.value[index].groupIds || []
    }
  } else {
    watchlist.value.unshift(fund)
  }
  persistCurrentSnapshot()
}

function deleteLocalFund(code: string) {
  watchlist.value = watchlist.value.filter((fund) => fund.code !== code)
  persistCurrentSnapshot()
}

function createLocalGroup(name: string): FundGroup {
  ensureLocalGroupNameAvailable(name)
  const group = { id: Date.now(), name, count: 0 }
  customGroups.value.push(group)
  persistCurrentSnapshot()
  return group
}

function updateLocalGroup(id: FundGroupId, name: string) {
  ensureLocalGroupNameAvailable(name, id)
  const group = customGroups.value.find((item) => item.id === id)
  if (group) {
    group.name = name
  }
  persistCurrentSnapshot()
}

function deleteLocalGroup(id: FundGroupId) {
  customGroups.value = customGroups.value.filter((group) => group.id !== id)
  watchlist.value.forEach((fund) => {
    fund.groupIds = (fund.groupIds || []).filter((groupId) => groupId !== id)
  })
  persistCurrentSnapshot()
}

function ensureLocalGroupNameAvailable(name: string, excludeId?: FundGroupId) {
  if (name === '全部' || name === '自选') {
    throw new Error('系统分组不能修改')
  }
  if (customGroups.value.some((group) => group.name === name && group.id !== excludeId)) {
    throw new Error('基金分组名称已存在')
  }
}

function normalizeSnapshot(snapshot: FundSnapshot): FundSnapshot {
  const groups = Array.isArray(snapshot?.groups) ? snapshot.groups : []
  const funds = Array.isArray(snapshot?.funds) ? snapshot.funds : []
  const groupIds = new Set(groups.map((group) => String(group.id)).filter(Boolean))
  return {
    groups: groups
      .filter((group) => group?.name)
      .map((group) => ({ id: String(group.id), name: String(group.name) })),
    funds: funds
      .filter((fund) => fund?.code)
      .map((fund) => ({
        code: String(fund.code),
        name: String(fund.name || fund.code),
        holdingAmount: Number(fund.holdingAmount || 0),
        holdingCost: Number(fund.holdingCost || 0),
        holdingCostNav: Number(fund.holdingCostNav || 0),
        holdingShares: Number(fund.holdingShares || 0),
        firstBuyDate: String(fund.firstBuyDate || ''),
        groupIds: (fund.groupIds || []).map((id) => String(id)).filter((id) => groupIds.has(id))
      })),
    transactions: Array.isArray(snapshot?.transactions)
      ? snapshot.transactions
          .filter((record) => record?.fundCode && record?.tradeType)
          .map((record) => ({
            id: record.id,
            fundCode: String(record.fundCode),
            fundName: String(record.fundName || record.fundCode),
            tradeType: String(record.tradeType),
            amount: Number(record.amount || 0),
            beforeAmount: Number(record.beforeAmount || 0),
            afterAmount: Number(record.afterAmount || 0),
            targetFundCode: record.targetFundCode,
            targetFundName: record.targetFundName,
            remark: record.remark,
            tradeTime: record.tradeTime || new Date().toISOString()
          }))
      : []
  }
}

function emptySnapshot(): FundSnapshot {
  return { funds: [], groups: [], transactions: [] }
}

function snapshotHasData(snapshot: FundSnapshot) {
  return snapshot.funds.length > 0 || snapshot.groups.length > 0 || snapshot.transactions.length > 0
}

function snapshotsEqual(left: FundSnapshot, right: FundSnapshot) {
  return JSON.stringify(snapshotComparable(left)) === JSON.stringify(snapshotComparable(right))
}

function snapshotComparable(snapshot: FundSnapshot) {
  return {
    funds: [...snapshot.funds]
      .map((fund) => ({
        code: fund.code,
        name: fund.name,
        holdingAmount: Number(fund.holdingAmount || 0),
        holdingCost: Number(fund.holdingCost || 0),
        holdingCostNav: Number(fund.holdingCostNav || 0),
        holdingShares: Number(fund.holdingShares || 0),
        firstBuyDate: fund.firstBuyDate || '',
        groupIds: [...(fund.groupIds || [])].map((id) => String(id)).sort((a, b) => a.localeCompare(b))
      }))
      .sort((a, b) => a.code.localeCompare(b.code)),
    groups: [...snapshot.groups]
      .map((group) => ({ id: String(group.id), name: group.name }))
      .sort((a, b) => a.id.localeCompare(b.id)),
    transactions: [...(snapshot.transactions || [])]
      .map((record) => ({
        fundCode: record.fundCode,
        fundName: record.fundName,
        tradeType: record.tradeType,
        amount: Number(record.amount || 0),
        beforeAmount: Number(record.beforeAmount || 0),
        afterAmount: Number(record.afterAmount || 0),
        targetFundCode: record.targetFundCode || '',
        targetFundName: record.targetFundName || '',
        tradeTime: record.tradeTime
      }))
      .sort((a, b) => `${a.tradeTime}${a.fundCode}${a.tradeType}`.localeCompare(`${b.tradeTime}${b.fundCode}${b.tradeType}`))
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
    selectedCodes.value = []
    searchOptions.value = []
    applySnapshotToPage(readLocalSnapshot())
    activeGroupKey.value = 'all'
    lastUpdated.value = watchlist.value.length ? '本地缓存' : ''
    unauthorized.value = false
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

function countFundsByGroup(groupId: FundGroupId) {
  return watchlist.value.filter((item) => (item.groupIds || []).map((id) => String(id)).includes(String(groupId))).length
}

function formatPercent(value: number) {
  return `${value >= 0 ? '+' : ''}${Number(value || 0).toFixed(2)}%`
}

function formatSignedNumber(value: number) {
  return `${value >= 0 ? '+' : ''}${Number(value || 0).toFixed(2)}`
}

function formatIndexPoint(value?: number) {
  return value === undefined || value === null ? '-' : Number(value || 0).toFixed(2)
}

function formatNumber(value?: number) {
  return value === undefined || value === null ? '-' : Number(value).toFixed(4)
}

function formatShares(value?: number) {
  return value === undefined || value === null ? '-' : Number(value || 0).toFixed(4)
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 2
  }).format(Number(value || 0))
}

function formatTradeTime(value: string) {
  if (!value) {
    return '-'
  }
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleString('zh-CN', { hour12: false })
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

<style scoped>
.group-manage-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.group-manage-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 36px;
}

.group-manage-main {
  flex: 1;
  min-width: 0;
}

.group-manage-row.is-system-group {
  color: var(--el-text-color-placeholder);
}

.group-manage-row.is-pending-delete {
  display: none;
}

.disabled-group-action {
  display: inline-flex;
}

.group-manage-footer {
  display: flex;
  justify-content: center;
  width: 100%;
}

.date-mode-button {
  margin-left: 8px;
  padding: 0;
}

.add-group-row {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 36px;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  color: var(--el-color-primary);
  background: transparent;
  cursor: pointer;
}

.add-group-row:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
</style>
