<template>
  <header class="app-header">
    <div class="app-header__brand" aria-label="基金预估">
      <span class="app-header__logo">
        <img :src="fundLogoUrl" alt="基金预估 Logo" />
      </span>
      <span class="app-header__brand-text">
        <strong>基金预估</strong>
        <small>Fund Insight Platform</small>
      </span>
    </div>

    <div
      class="app-header__search"
      :class="{ 'app-header__search--active': isSearchActive }"
      role="search"
      @focusin="isSearchActive = true"
      @focusout="handleSearchFocusOut"
    >
      <el-icon class="app-header__search-icon" :size="18">
        <Search />
      </el-icon>
      <input v-model="keyword" type="search" placeholder="搜索基金名称或代码…" aria-label="搜索基金名称或代码" />
      <button
        class="app-header__icon-button app-header__search-action"
        type="button"
        :aria-label="isSearchActive ? '添加基金' : '图像识别搜索'"
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
      <button class="app-header__avatar-button" type="button" aria-label="用户中心">
        <img :src="userAvatarUrl" alt="用户头像" />
      </button>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Camera, Connection, Moon, Plus, RefreshRight, Search, Sunny } from '@element-plus/icons-vue'

import fundLogoUrl from '@/assets/logo/fund-logo.svg'
import userAvatarUrl from '@/assets/logo/user-avatar.svg'
import { useTheme } from '@/composables/useTheme'

const keyword = ref('')
const isSearchActive = ref(false)
const { currentThemeLabel, isDarkTheme, toggleTheme } = useTheme()

const handleSearchFocusOut = (event: FocusEvent): void => {
  const nextTarget = event.relatedTarget
  if (!(event.currentTarget instanceof HTMLElement) || !(nextTarget instanceof Node)) {
    isSearchActive.value = false
    return
  }

  isSearchActive.value = event.currentTarget.contains(nextTarget)
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
  overflow: hidden;

  &::before {
    position: absolute;
    inset: 0;
    pointer-events: none;
    content: "";
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
    width: min(100%, 560px);
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
