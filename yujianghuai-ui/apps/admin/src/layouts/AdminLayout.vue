<template>
  <div class="admin-app">
    <aside class="sidebar">
      <div class="admin-brand">
        <span class="admin-brand-mark">Y</span>
        <span>基金实时预估V1.0</span>
      </div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>权限管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单权限</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <main class="admin-main">
      <header class="topbar">
        <div>
          <strong>{{ title }}</strong>
          <div style="color: #728079; margin-top: 4px">基金实时预估V1.0 后台管理</div>
        </div>
        <el-space>
          <el-tag type="success" effect="dark">ADMIN</el-tag>
          <el-button round @click="logout">退出</el-button>
        </el-space>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const title = computed(() => {
  const map: Record<string, string> = {
    '/dashboard': '基金实时预估V1.0',
    '/system/user': '基金实时预估V1.0 · 用户管理',
    '/system/role': '基金实时预估V1.0 · 角色管理',
    '/system/menu': '基金实时预估V1.0 · 菜单权限'
  }
  return map[route.path] ?? '基金实时预估V1.0'
})

function logout() {
  localStorage.removeItem('YJH_ADMIN_AUTH')
  localStorage.removeItem('YJH_TOKEN')
  router.replace('/login')
}
</script>
