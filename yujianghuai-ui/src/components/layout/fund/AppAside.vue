<template>
  <aside class="app-aside" aria-label="基金功能菜单">
    <div class="app-aside__heading">
      <span class="app-aside__eyebrow">功能菜单</span>
      <strong>盘中工作台</strong>
    </div>

    <div v-if="isLoading" class="app-aside__skeleton" aria-label="菜单加载中">
      <span v-for="item in 5" :key="item"></span>
    </div>

    <div v-else class="app-aside__section">
      <p v-if="loadError" class="app-aside__notice">{{ loadError }}</p>
      <button
        v-for="item in menuItems"
        :key="item.key"
        class="app-aside__item"
        :class="{ 'app-aside__item--active': item.key === activeKey }"
        type="button"
        @click="$emit('select', item.key)"
      >
        <span class="app-aside__item-icon" aria-hidden="true">{{ item.shortLabel }}</span>
        <span class="app-aside__item-copy">
          <strong>{{ item.title }}</strong>
          <small>{{ item.description }}</small>
        </span>
        <span v-if="item.badge" class="app-aside__item-badge">{{ item.badge }}</span>
      </button>
    </div>

    <div class="app-aside__status" aria-label="菜单同步状态">
      <span></span>
      <div>
        <strong>{{ isLoading ? '同步中' : '菜单已同步' }}</strong>
        <small>{{ statusText }}</small>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
export interface AsideMenuItem {
  key: string
  title: string
  description: string
  shortLabel: string
  badge?: string
}

defineProps<{
  activeKey: string
  menuItems: AsideMenuItem[]
  isLoading: boolean
  loadError: string
  statusText: string
}>()

defineEmits<{
  select: [key: string]
}>()
</script>

<style scoped lang="scss">
.app-aside {
  position: sticky;
  top: 104px;
  display: grid;
  align-self: start;
  gap: 18px;
  min-height: 540px;
  padding: 18px;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  color: var(--text-color);
  background: var(--aside-bg);
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(18px);
}

.app-aside__heading {
  display: grid;
  gap: 6px;

  strong {
    font-size: 18px;
    line-height: 1.2;
  }
}

.app-aside__eyebrow {
  color: var(--text-subtle);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.2;
}

.app-aside__section,
.app-aside__skeleton {
  display: grid;
  gap: 8px;
  align-content: start;
}

.app-aside__skeleton span {
  height: 54px;
  border-radius: 12px;
  background: linear-gradient(90deg, var(--button-bg), var(--hover-bg), var(--button-bg));
  background-size: 200% 100%;
  animation: aside-skeleton 1.2s ease-in-out infinite;
}

.app-aside__notice {
  margin: 0 0 4px;
  padding: 10px 12px;
  border: 1px solid rgba(var(--primary-color-rgb), 0.22);
  border-radius: 12px;
  color: var(--text-muted);
  background: rgba(var(--primary-color-rgb), 0.08);
  font-size: 12px;
  line-height: 1.5;
}

.app-aside__item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 54px;
  padding: 8px 10px;
  border: 1px solid transparent;
  border-radius: 12px;
  color: var(--text-muted);
  text-align: left;
  background: transparent;
  cursor: pointer;
  transition:
    background-color 180ms ease,
    color 180ms ease,
    border-color 180ms ease,
    transform 180ms ease;

  &:hover,
  &:focus-visible {
    color: var(--text-color);
    background: var(--hover-bg);
    border-color: var(--border-color-strong);
    outline: 0;
    transform: translateY(-1px);
  }
}

.app-aside__item--active {
  color: var(--text-color);
  background: rgba(var(--primary-color-rgb), 0.12);
  border-color: rgba(var(--primary-color-rgb), 0.34);

  .app-aside__item-icon {
    color: var(--bg-color);
    background: var(--primary-color);
  }
}

.app-aside__item-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border-color-strong);
  border-radius: 11px;
  color: var(--primary-color);
  background: var(--button-bg);
  font-size: 12px;
  font-weight: 800;
}

.app-aside__item-copy {
  display: grid;
  gap: 4px;
  min-width: 0;

  strong,
  small {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: currentColor;
    font-size: 14px;
    line-height: 1.1;
  }

  small {
    color: var(--text-subtle);
    font-size: 12px;
    line-height: 1.2;
  }
}

.app-aside__item-badge {
  min-width: 24px;
  padding: 4px 7px;
  border-radius: 999px;
  color: var(--primary-color);
  background: rgba(var(--primary-color-rgb), 0.1);
  font-size: 11px;
  font-weight: 800;
  text-align: center;
}

.app-aside__status {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  margin-top: auto;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: var(--button-bg);

  > span {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: var(--primary-color);
    box-shadow: 0 0 0 5px rgba(var(--primary-color-rgb), 0.1);
  }

  div {
    display: grid;
    gap: 3px;
    min-width: 0;
  }

  strong {
    font-size: 13px;
    line-height: 1.1;
  }

  small {
    overflow: hidden;
    color: var(--text-subtle);
    font-size: 12px;
    line-height: 1.2;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@keyframes aside-skeleton {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

@media (max-width: 960px) {
  .app-aside {
    position: static;
    min-height: auto;
  }

  .app-aside__section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .app-aside__section {
    grid-template-columns: 1fr;
  }
}
</style>
