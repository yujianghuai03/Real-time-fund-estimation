<template>
  <footer class="app-footer">
    <span>{{ statusText }}</span>
    <span>{{ activeTitle }} · Footer 组件预留</span>
  </footer>
</template>

<script setup lang="ts">
defineProps<{
  activeTitle: string
  statusText: string
}>()
</script>

<style scoped lang="scss">
.app-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  width: min(1440px, calc(100% - 32px));
  margin: 0 auto 20px;
  padding: 14px 18px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  color: var(--text-muted);
  background: var(--footer-bg);
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(18px);
  font-size: 13px;
}

@media (max-width: 640px) {
  .app-footer {
    flex-direction: column;
  }
}
</style>
