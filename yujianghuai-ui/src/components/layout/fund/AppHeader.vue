<template>
  <header class="app-header">
    <div class="app-header__brand" aria-label="基金智估">
      <span class="app-header__logo">
        <img :src="fundLogoUrl" alt="基金智估 Logo" />
      </span>
      <span class="app-header__brand-text">
        <strong>基金智估</strong>
        <small>Fund Insight Platform</small>
      </span>
    </div>

    <div
      class="app-header__search"
      :class="{ 'app-header__search--active': isSearchActive }"
      role="search"
    >
      <el-icon class="app-header__search-icon" :size="18">
        <Search />
      </el-icon>
      <input
        v-model="keyword"
        type="search"
        placeholder="搜索基金名称或代码…"
        aria-label="搜索基金名称或代码"
        @focus="isSearchActive = true"
        @blur="isSearchActive = false"
      />
      <button
        class="app-header__icon-button app-header__search-action"
        type="button"
        :aria-label="isSearchActive ? '添加基金' : '图像识别搜索'"
        @mousedown.prevent
        @click="handleSearchActionClick"
      >
        <Transition name="app-header__search-action-icon" mode="out-in">
          <el-icon :key="isSearchActive ? 'plus' : 'camera'" :size="18">
            <Plus v-if="isSearchActive" />
            <Camera v-else />
          </el-icon>
        </Transition>
      </button>
    </div>

    <nav class="app-header__actions" aria-label="快捷功能">
      <button class="app-header__icon-button" type="button" aria-label="GitHub">
        <el-icon :size="18">
          <Connection />
        </el-icon>
      </button>
      <button class="app-header__icon-button" type="button" aria-label="刷新数据" @click="refreshHeader">
        <el-icon :size="18">
          <RefreshRight />
        </el-icon>
      </button>
      <button
        class="app-header__icon-button app-header__theme-button"
        type="button"
        :aria-label="`切换主题，当前为${currentThemeLabel}`"
        :title="`当前主题：${currentThemeLabel}`"
        @click="toggleTheme"
      >
        <el-icon :size="18">
          <Moon v-if="isDarkTheme" />
          <Sunny v-else />
        </el-icon>
      </button>
      <div ref="userMenuRef" class="app-header__user-menu">
        <button
          class="app-header__avatar-button"
          type="button"
          :aria-label="isUserLoggedIn ? '已登录用户中心' : '未登录用户中心'"
          :aria-expanded="isUserMenuVisible"
          aria-haspopup="menu"
          @click="toggleUserMenu"
        >
          <img :src="userAvatarUrl" alt="用户头像" />
        </button>

        <Transition name="app-header__user-dropdown">
          <div
            v-if="isUserMenuVisible"
            class="app-header__user-dropdown"
            :class="{ 'app-header__user-dropdown--logged-in': isUserLoggedIn }"
            role="menu"
            :aria-label="isUserLoggedIn ? '已登录用户菜单' : '未登录用户菜单'"
          >
            <template v-if="isUserLoggedIn">
              <div class="app-header__user-profile" role="presentation">
                <img :src="userAvatarUrl" alt="" />
                <div class="app-header__user-profile-copy">
                  <strong>{{ userProfile.email }}</strong>
                  <span class="app-header__user-status">已登录</span>
                  <small>同步于 {{ userProfile.syncedAt }}</small>
                </div>
              </div>

              <div class="app-header__user-menu-divider" role="presentation"></div>
            </template>

            <button
              v-if="!isUserLoggedIn"
              class="app-header__user-menu-item"
              type="button"
              role="menuitem"
              @click="openLoginDialog"
            >
              <el-icon :size="16">
                <Right />
              </el-icon>
              <span>登录</span>
            </button>

            <button class="app-header__user-menu-item" type="button" role="menuitem" @click="handleUserMenuAction">
              <el-icon :size="16">
                <Calendar />
              </el-icon>
              <span>我的收益</span>
            </button>
            <button
              v-if="isUserLoggedIn"
              class="app-header__user-menu-item app-header__user-menu-item--sync"
              type="button"
              role="menuitem"
              @click="handleUserMenuAction"
            >
              <el-icon :size="16">
                <Refresh />
              </el-icon>
              <span>同步</span>
            </button>
            <button class="app-header__user-menu-item" type="button" role="menuitem" @click="handleUserMenuAction">
              <el-icon :size="16">
                <Setting />
              </el-icon>
              <span>设置</span>
            </button>
            <button
              v-if="isUserLoggedIn"
              class="app-header__user-menu-item app-header__user-menu-item--danger"
              type="button"
              role="menuitem"
              @click="handleUserMenuAction"
            >
              <el-icon :size="16">
                <SwitchButton />
              </el-icon>
              <span>登出</span>
            </button>
          </div>
        </Transition>
      </div>
    </nav>
  </header>

  <Teleport to="body">
    <Transition name="app-header__capture-dialog">
      <div
        v-if="isCaptureDialogVisible"
        class="app-header__capture-overlay"
        role="presentation"
        @click.self="closeCaptureDialog"
      >
        <section
          class="app-header__capture-card"
          role="dialog"
          aria-modal="true"
          aria-labelledby="capture-dialog-title"
        >
          <h2 id="capture-dialog-title">选择持仓截图</h2>
          <p>
            从相册选择一张或多张持仓截图，系统将自动识别其中的
            <strong>基金名称或基金代码（6位数字）</strong>
            ，并支持批量导入。
          </p>

          <button class="app-header__capture-dropzone" type="button" @click="triggerImageSelect">
            拖拽图片到此处，或点击选择
          </button>
          <input
            ref="captureInputRef"
            class="app-header__capture-input"
            type="file"
            accept="image/*"
            multiple
          />

          <footer class="app-header__capture-actions">
            <button class="app-header__capture-button" type="button" @click="closeCaptureDialog">取消</button>
            <button
              class="app-header__capture-button app-header__capture-button--primary"
              type="button"
              @click="triggerImageSelect"
            >
              选择图片
            </button>
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>

  <Teleport to="body">
    <Transition name="app-header__login-dialog">
      <div
        v-if="isLoginDialogVisible"
        class="app-header__login-overlay"
        role="presentation"
        @click.self="closeLoginDialog"
      >
        <section class="app-header__login-card" role="dialog" aria-modal="true" aria-labelledby="login-dialog-title">
          <button class="app-header__login-close" type="button" aria-label="关闭登录弹窗" @click="closeLoginDialog">
            <el-icon :size="18">
              <Close />
            </el-icon>
          </button>

          <div class="app-header__login-heading">
            <span class="app-header__login-logo">
              <img :src="fundLogoUrl" alt="" />
            </span>
            <div>
              <h2 id="login-dialog-title">基金智估</h2>
              <p>{{ loginDialogSubtitle }}</p>
            </div>
          </div>

          <div class="app-header__login-tabs" role="tablist" aria-label="账号操作">
            <button
              type="button"
              role="tab"
              :aria-selected="loginMode === 'login'"
              :class="{ 'app-header__login-tab--active': loginMode === 'login' }"
              @click="setLoginMode('login')"
            >
              登录
            </button>
            <button
              type="button"
              role="tab"
              :aria-selected="loginMode === 'register'"
              :class="{ 'app-header__login-tab--active': loginMode === 'register' }"
              @click="setLoginMode('register')"
            >
              创建账号
            </button>
          </div>

          <form class="app-header__login-form" @submit.prevent="handleLoginSubmit">
            <label class="app-header__login-field">
              <span>邮箱</span>
              <div class="app-header__login-input">
                <el-icon :size="17">
                  <Message />
                </el-icon>
                <input v-model="loginForm.email" type="email" placeholder="请输入邮箱" autocomplete="email" />
              </div>
            </label>

            <label v-if="loginMode !== 'forgot'" class="app-header__login-field">
              <span>密码</span>
              <div class="app-header__login-input">
                <el-icon :size="17">
                  <Lock />
                </el-icon>
                <input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                />
              </div>
            </label>

            <label v-if="loginMode === 'register'" class="app-header__login-field">
              <span>确认密码</span>
              <div class="app-header__login-input">
                <el-icon :size="17">
                  <Lock />
                </el-icon>
                <input
                  v-model="loginForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  autocomplete="new-password"
                />
              </div>
            </label>

            <div v-if="loginMode === 'login'" class="app-header__login-meta">
              <label>
                <input v-model="loginForm.remember" type="checkbox" />
                <span>保持登录</span>
              </label>
              <button type="button" @click="setLoginMode('forgot')">忘记密码</button>
            </div>

            <p v-if="loginMode === 'forgot'" class="app-header__login-hint">
              输入邮箱后，我们会发送重置密码链接到你的邮箱。
            </p>

            <button class="app-header__login-submit" type="submit">{{ loginSubmitText }}</button>
          </form>

          <div v-if="loginMode !== 'forgot'" class="app-header__login-divider" role="presentation">
            <span>其他方式登录</span>
          </div>

          <div v-if="loginMode !== 'forgot'" class="app-header__login-providers">
            <button type="button">
              <el-icon :size="16">
                <Connection />
              </el-icon>
              GitHub
            </button>
            <button type="button">
              <el-icon :size="16">
                <User />
              </el-icon>
              手机验证码
            </button>
          </div>

          <footer class="app-header__login-footer">
            <template v-if="loginMode === 'login'">
              <span>还没有账号？</span>
              <button type="button" @click="setLoginMode('register')">创建账号</button>
            </template>
            <template v-else>
              <span>{{ loginMode === 'register' ? '已有账号？' : '想起密码了？' }}</span>
              <button type="button" @click="setLoginMode('login')">返回登录</button>
            </template>
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, watch, ref } from 'vue'
import {
  Calendar,
  Camera,
  Close,
  Connection,
  Lock,
  Message,
  Moon,
  Plus,
  Refresh,
  RefreshRight,
  Right,
  Search,
  Setting,
  Sunny,
  SwitchButton,
  User,
} from '@element-plus/icons-vue'

import fundLogoUrl from '@/assets/logo/fund-logo.svg'
import userAvatarUrl from '@/assets/logo/user-avatar.svg'
import { useTheme } from '@/composables/useTheme'

const keyword = ref('')
const isSearchActive = ref(false)
const isCaptureDialogVisible = ref(false)
const isLoginDialogVisible = ref(false)
const isUserMenuVisible = ref(false)
const isUserLoggedIn = ref(false)
const captureInputRef = ref<HTMLInputElement | null>(null)
const userMenuRef = ref<HTMLElement | null>(null)
const { currentThemeLabel, isDarkTheme, toggleTheme } = useTheme()
const userProfile = {
  email: 'xhh64481@gmail.com',
  syncedAt: '05-07 14:56',
}
type LoginMode = 'login' | 'forgot' | 'register'

const loginMode = ref<LoginMode>('login')
const loginForm = ref({
  email: '',
  password: '',
  confirmPassword: '',
  remember: true,
})

const loginDialogSubtitle = computed(() => {
  if (loginMode.value === 'forgot') {
    return '找回账号密码'
  }

  if (loginMode.value === 'register') {
    return '创建账号并同步基金数据'
  }

  return '同步持仓收益与自选基金'
})

const loginSubmitText = computed(() => {
  if (loginMode.value === 'forgot') {
    return '发送重置链接'
  }

  if (loginMode.value === 'register') {
    return '创建账号'
  }

  return '登录'
})

const handleSearchActionClick = (): void => {
  if (isSearchActive.value) {
    return
  }

  isCaptureDialogVisible.value = true
}

const closeCaptureDialog = (): void => {
  isCaptureDialogVisible.value = false
}

const closeLoginDialog = (): void => {
  isLoginDialogVisible.value = false
}

const closeUserMenu = (): void => {
  isUserMenuVisible.value = false
}

const toggleUserMenu = (): void => {
  isUserMenuVisible.value = !isUserMenuVisible.value
}

const handleUserMenuAction = (): void => {
  closeUserMenu()
}

const setLoginMode = (mode: LoginMode): void => {
  loginMode.value = mode
}

const openLoginDialog = (): void => {
  closeUserMenu()
  setLoginMode('login')
  isLoginDialogVisible.value = true
}

const handleLoginSubmit = (): void => {
  closeLoginDialog()
}

const handleGlobalKeydown = (event: KeyboardEvent): void => {
  if (event.key === 'Escape') {
    closeCaptureDialog()
    closeLoginDialog()
    closeUserMenu()
  }
}

const handleGlobalPointerdown = (event: PointerEvent): void => {
  if (!isUserMenuVisible.value) {
    return
  }

  const target = event.target
  if (target instanceof Node && userMenuRef.value?.contains(target)) {
    return
  }

  closeUserMenu()
}

watch(
  () => isCaptureDialogVisible.value || isLoginDialogVisible.value || isUserMenuVisible.value,
  (isVisible) => {
    if (isVisible) {
      window.addEventListener('keydown', handleGlobalKeydown)
      return
    }

    window.removeEventListener('keydown', handleGlobalKeydown)
  }
)

watch(isUserMenuVisible, (isVisible) => {
  if (isVisible) {
    document.addEventListener('pointerdown', handleGlobalPointerdown)
    return
  }

  document.removeEventListener('pointerdown', handleGlobalPointerdown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
  document.removeEventListener('pointerdown', handleGlobalPointerdown)
})

const triggerImageSelect = (): void => {
  captureInputRef.value?.click()
}

const refreshHeader = (): void => {
  // Header 只触发交互，后续可在父组件或 store 中接入真实刷新逻辑。
}
</script>

<style scoped lang="scss">
.app-header {
  position: sticky;
  top: 12px;
  z-index: 10;
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(320px, 560px) minmax(220px, 1fr);
  align-items: center;
  gap: 24px;
  width: calc(100% - 24px);
  min-height: 65px;
  margin: 0 auto;
  padding: 8px clamp(18px, 4vw, 36px);
  color: var(--text-color);
  background: var(--header-bg);
  border: 1px solid var(--border-color);
  border-radius: 18px;
  box-shadow: var(--shadow-soft), 0 8px 28px var(--shadow-color);
  backdrop-filter: blur(18px);
  overflow: visible;
  isolation: isolate;

  &::before {
    position: absolute;
    inset: 0;
    pointer-events: none;
    content: "";
    z-index: 0;
    border-radius: inherit;
    background:
      linear-gradient(120deg, rgba(var(--text-color-rgb), 0.1), transparent 34%),
      radial-gradient(circle at 38% 20%, rgba(var(--primary-color-rgb), 0.18), transparent 26%),
      radial-gradient(circle at 72% 70%, rgba(var(--primary-color-rgb), 0.1), transparent 24%);
    opacity: 0.9;
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

.app-header__brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.app-header__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  flex: 0 0 46px;
  border: 1px solid var(--border-color-strong);
  border-radius: 50%;
  background: var(--logo-bg);
  box-shadow: 0 10px 24px var(--shadow-color), inset 0 1px 0 rgba(var(--text-color-rgb), 0.16);

  img {
    width: 36px;
    height: 36px;
    display: block;
  }
}

.app-header__brand-text {
  display: grid;
  gap: 3px;
  min-width: 0;

  strong {
    font-size: 20px;
    line-height: 1;
    font-weight: 700;
    white-space: nowrap;
  }

  small {
    color: var(--text-muted);
    font-size: 11px;
    line-height: 1;
    letter-spacing: 0;
    white-space: nowrap;
  }
}

.app-header__search {
  justify-self: center;
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr) 34px;
  align-items: center;
  width: min(100%, 380px);
  height: 44px;
  padding: 0 5px 0 15px;
  border: 1px solid var(--border-color-strong);
  border-radius: 15px;
  background: var(--input-bg);
  box-shadow: inset 0 1px 0 rgba(var(--text-color-rgb), 0.08), 0 10px 24px var(--shadow-color);
  backdrop-filter: blur(16px);
  transition:
    width 240ms ease,
    border-color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;

  &:focus-within {
    border-color: rgba(var(--primary-color-rgb), 0.42);
    background: var(--card-bg-strong);
    box-shadow:
      inset 0 1px 0 rgba(var(--text-color-rgb), 0.1),
      0 0 0 4px var(--focus-ring),
      0 10px 24px var(--shadow-color);
  }

  input {
    width: 100%;
    min-width: 0;
    border: 0;
    outline: 0;
    color: var(--text-color);
    background: transparent;
    font-size: 14px;

    &::placeholder {
      color: var(--placeholder-color);
    }

    &::-webkit-search-cancel-button {
      appearance: none;
    }
  }
}

.app-header__search--active {
  width: min(100%, 560px);
}

.app-header__capture-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(2, 8, 23, 0.28);
  backdrop-filter: blur(3px);
}

.app-header__capture-card {
  width: min(420px, 100%);
  padding: 18px;
  color: var(--text-color);
  background: var(--header-bg);
  border: 1px solid var(--border-color-strong);
  border-radius: 18px;
  box-shadow: var(--shadow-card), 0 0 0 1px rgba(var(--text-color-rgb), 0.02);
  transition:
    background-color 0.3s ease,
    color 0.3s ease,
    border-color 0.3s ease,
    box-shadow 0.3s ease;

  h2 {
    margin: 0 0 14px;
    font-size: 16px;
    line-height: 1.25;
    font-weight: 700;
  }

  p {
    margin: 0 0 14px;
    color: var(--text-muted);
    font-size: 13px;
    line-height: 1.7;
  }

  strong {
    color: var(--primary-color);
    font-weight: 700;
  }
}

.app-header__capture-dropzone {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 62px;
  margin: 0;
  border: 1px dashed var(--border-color-strong);
  border-radius: 12px;
  color: var(--text-muted);
  background: rgba(var(--text-color-rgb), 0.018);
  cursor: pointer;
  transition:
    border-color 180ms ease,
    background-color 180ms ease,
    color 180ms ease;

  &:hover {
    color: var(--text-color);
    border-color: rgba(var(--primary-color-rgb), 0.42);
    background: var(--hover-bg);
  }
}

.app-header__capture-input {
  display: none;
}

.app-header__capture-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}

.app-header__capture-button {
  min-width: 64px;
  height: 44px;
  padding: 0 16px;
  border: 1px solid var(--border-color-strong);
  border-radius: 12px;
  color: var(--text-color);
  background: var(--button-bg);
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease,
    color 180ms ease;

  &:hover {
    border-color: rgba(var(--primary-color-rgb), 0.34);
    background: var(--hover-bg);
    transform: translateY(-1px);
  }
}

.app-header__capture-button--primary {
  color: #05111f;
  border-color: transparent;
  background: var(--primary-color);
  box-shadow: 0 10px 20px rgba(var(--primary-color-rgb), 0.24);
}

.app-header__capture-dialog-enter-active,
.app-header__capture-dialog-leave-active {
  transition: opacity 180ms ease;

  .app-header__capture-card {
    transition:
      opacity 180ms ease,
      transform 180ms ease;
  }
}

.app-header__capture-dialog-enter-from,
.app-header__capture-dialog-leave-to {
  opacity: 0;

  .app-header__capture-card {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }
}

.app-header__login-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(2, 8, 23, 0.34);
  backdrop-filter: blur(6px);
}

.app-header__login-card {
  position: relative;
  width: min(430px, 100%);
  padding: 24px;
  color: var(--text-color);
  background: var(--card-bg-strong);
  border: 1px solid var(--border-color-strong);
  border-radius: 18px;
  box-shadow: var(--shadow-card), inset 0 1px 0 rgba(var(--text-color-rgb), 0.08);
  backdrop-filter: blur(22px);
}

.app-header__login-close {
  position: absolute;
  top: 14px;
  right: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border-color-strong);
  border-radius: 11px;
  color: var(--text-muted);
  background: var(--button-bg);
  cursor: pointer;
  transition:
    color 160ms ease,
    border-color 160ms ease,
    background-color 160ms ease,
    transform 160ms ease;

  &:hover,
  &:focus-visible {
    color: var(--text-color);
    border-color: rgba(var(--primary-color-rgb), 0.34);
    background: var(--hover-bg);
    outline: 0;
    transform: translateY(-1px);
  }
}

.app-header__login-heading {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding-right: 42px;
  margin-bottom: 18px;

  h2 {
    margin: 0 0 5px;
    font-size: 22px;
    line-height: 1.1;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: var(--text-muted);
    font-size: 13px;
    line-height: 1.4;
  }
}

.app-header__login-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border: 1px solid var(--border-color-strong);
  border-radius: 50%;
  background: var(--logo-bg);
  box-shadow: inset 0 1px 0 rgba(var(--text-color-rgb), 0.14), 0 10px 24px var(--shadow-color);

  img {
    width: 36px;
    height: 36px;
    display: block;
  }
}

.app-header__login-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  height: 42px;
  padding: 4px;
  margin-bottom: 16px;
  border: 1px solid var(--border-color-strong);
  border-radius: 13px;
  background: var(--input-bg);

  button {
    border: 0;
    border-radius: 9px;
    color: var(--text-muted);
    background: transparent;
    cursor: pointer;
    font-size: 13px;
    font-weight: 800;
    transition:
      color 160ms ease,
      background-color 160ms ease,
      box-shadow 160ms ease;

    &:focus-visible {
      outline: 0;
      box-shadow: 0 0 0 3px var(--focus-ring);
    }
  }
}

.app-header__login-tab--active {
  color: var(--text-color) !important;
  background: var(--card-bg-strong) !important;
  box-shadow: inset 0 1px 0 rgba(var(--text-color-rgb), 0.08), 0 6px 18px var(--shadow-color);
}

.app-header__login-form {
  display: grid;
  gap: 14px;
}

.app-header__login-field {
  display: grid;
  gap: 8px;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 700;
}

.app-header__login-input {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  height: 44px;
  padding: 0 13px;
  border: 1px solid var(--border-color-strong);
  border-radius: 13px;
  color: var(--text-muted);
  background: var(--input-bg);
  box-shadow: inset 0 1px 0 rgba(var(--text-color-rgb), 0.06);
  transition:
    border-color 160ms ease,
    background-color 160ms ease,
    box-shadow 160ms ease;

  &:focus-within {
    border-color: rgba(var(--primary-color-rgb), 0.42);
    background: var(--card-bg-strong);
    box-shadow: 0 0 0 4px var(--focus-ring), inset 0 1px 0 rgba(var(--text-color-rgb), 0.08);
  }

  input {
    width: 100%;
    min-width: 0;
    border: 0;
    outline: 0;
    color: var(--text-color);
    background: transparent;
    font-size: 14px;

    &::placeholder {
      color: var(--placeholder-color);
    }
  }
}

.app-header__login-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-muted);
  font-size: 13px;

  label {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    cursor: pointer;
  }

  input {
    accent-color: var(--primary-color);
  }

  button {
    border: 0;
    color: var(--primary-color);
    background: transparent;
    cursor: pointer;
    font: inherit;
  }
}

.app-header__login-hint {
  margin: -2px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.app-header__login-submit {
  height: 44px;
  border: 0;
  border-radius: 13px;
  color: #05111f;
  background: var(--primary-color);
  box-shadow: 0 12px 24px rgba(var(--primary-color-rgb), 0.24);
  cursor: pointer;
  font-size: 15px;
  font-weight: 800;
  transition:
    filter 160ms ease,
    transform 160ms ease;

  &:hover,
  &:focus-visible {
    filter: brightness(1.05);
    outline: 0;
    transform: translateY(-1px);
  }
}

.app-header__login-divider {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 10px;
  margin: 18px 0 12px;
  color: var(--text-subtle);
  font-size: 12px;

  &::before,
  &::after {
    height: 1px;
    content: "";
    background: var(--border-color-strong);
  }
}

.app-header__login-providers {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;

  button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 7px;
    min-width: 0;
    height: 40px;
    border: 1px solid var(--border-color-strong);
    border-radius: 12px;
    color: var(--text-color);
    background: var(--button-bg);
    cursor: pointer;
    font-size: 13px;
    font-weight: 700;
    transition:
      border-color 160ms ease,
      background-color 160ms ease,
      transform 160ms ease;

    &:hover,
    &:focus-visible {
      border-color: rgba(var(--primary-color-rgb), 0.34);
      background: var(--hover-bg);
      outline: 0;
      transform: translateY(-1px);
    }
  }
}

.app-header__login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  color: var(--text-muted);
  font-size: 13px;

  button {
    border: 0;
    color: var(--primary-color);
    background: transparent;
    cursor: pointer;
    font: inherit;
    font-weight: 700;
  }
}

.app-header__login-dialog-enter-active,
.app-header__login-dialog-leave-active {
  transition: opacity 180ms ease;

  .app-header__login-card {
    transition:
      opacity 180ms ease,
      transform 180ms ease;
  }
}

.app-header__login-dialog-enter-from,
.app-header__login-dialog-leave-to {
  opacity: 0;

  .app-header__login-card {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
}

.app-header__search-icon {
  color: var(--text-muted);
}

.app-header__actions {
  justify-self: end;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.app-header__icon-button,
.app-header__avatar-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color-strong);
  color: var(--text-color);
  background: var(--button-bg);
  box-shadow: inset 0 1px 0 rgba(var(--text-color-rgb), 0.08);
  backdrop-filter: blur(14px);
  cursor: pointer;
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease,
    color 180ms ease;

  &:hover {
    color: var(--text-color);
    background: var(--hover-bg);
    border-color: rgba(var(--primary-color-rgb), 0.32);
    box-shadow: inset 0 1px 0 rgba(var(--text-color-rgb), 0.14), 0 8px 20px var(--shadow-color);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

.app-header__icon-button {
  width: 38px;
  height: 38px;
  border-radius: 13px;
}

.app-header__search-action {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  color: var(--primary-color);
  overflow: hidden;
}

.app-header__search-action-icon-enter-active,
.app-header__search-action-icon-leave-active {
  transition:
    opacity 140ms ease,
    transform 140ms ease;
}

.app-header__search-action-icon-enter-from,
.app-header__search-action-icon-leave-to {
  opacity: 0;
  transform: scale(0.72) rotate(-12deg);
}

.app-header__theme-button {
  color: var(--primary-color);
}

.app-header__user-menu {
  position: relative;
  display: inline-flex;
}

.app-header__avatar-button {
  width: 40px;
  height: 40px;
  padding: 2px;
  border-radius: 50%;

  img {
    width: 100%;
    height: 100%;
    display: block;
    border-radius: inherit;
  }
}

.app-header__user-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  z-index: 20;
  display: grid;
  width: 200px;
  padding: 10px 8px;
  border: 1px solid var(--border-color-strong);
  border-radius: 12px;
  background: var(--card-bg-strong);
  box-shadow: var(--shadow-card), inset 0 1px 0 rgba(var(--text-color-rgb), 0.06);
  backdrop-filter: blur(18px);
}

.app-header__user-dropdown--logged-in {
  width: 228px;
  padding: 12px 8px 10px;
}

.app-header__user-profile {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  padding: 4px 8px 10px;

  img {
    width: 40px;
    height: 40px;
    display: block;
    border-radius: 50%;
  }
}

.app-header__user-profile-copy {
  display: grid;
  gap: 3px;
  min-width: 0;

  strong {
    overflow: hidden;
    color: var(--text-color);
    font-size: 14px;
    line-height: 1.2;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  small {
    color: var(--text-muted);
    font-size: 11px;
    line-height: 1.2;
  }
}

.app-header__user-status {
  color: #00a870;
  font-size: 11px;
  line-height: 1.2;
}

.app-header__user-menu-divider {
  height: 1px;
  margin: 8px 0 8px;
  background: var(--border-color-strong);
}

.app-header__user-menu-item {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 42px;
  padding: 0 12px;
  border-radius: 8px;
  border: 0;
  color: var(--text-color);
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  text-align: left;
  transition:
    background-color 160ms ease,
    color 160ms ease;

  .el-icon {
    color: var(--text-color);
  }

  &:hover,
  &:focus-visible {
    color: var(--text-color);
    background: var(--hover-bg);
    outline: 0;
  }
}

.app-header__user-dropdown--logged-in .app-header__user-menu-item {
  min-height: 40px;
}

.app-header__user-menu-item--sync .el-icon {
  color: var(--primary-color);
}

.app-header__user-menu-item--danger {
  color: #ff4d4f;

  .el-icon {
    color: #ff4d4f;
  }

  &:hover,
  &:focus-visible {
    color: #ff4d4f;
    background: rgba(255, 77, 79, 0.1);
  }
}

.app-header__user-dropdown-enter-active,
.app-header__user-dropdown-leave-active {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}

.app-header__user-dropdown-enter-from,
.app-header__user-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}

@media (max-width: 920px) {
  .app-header {
    grid-template-columns: 1fr auto;
    min-height: auto;
    padding: 10px 18px;
  }

  .app-header__search {
    grid-column: 1 / -1;
    grid-row: 2;
    width: 100%;
  }

  .app-header__actions {
    gap: 6px;
  }
}

@media (max-width: 560px) {
  .app-header {
    gap: 14px;
  }

  .app-header__brand-text small {
    display: none;
  }

  .app-header__logo {
    width: 40px;
    height: 40px;
    flex-basis: 40px;

    img {
      width: 30px;
      height: 30px;
    }
  }

  .app-header__brand-text strong {
    font-size: 18px;
  }

  .app-header__actions {
    gap: 5px;
  }

  .app-header__icon-button {
    width: 34px;
    height: 34px;
    border-radius: 11px;
  }

  .app-header__avatar-button {
    width: 36px;
    height: 36px;
  }

  .app-header__search {
    height: 40px;
  }
}
</style>
