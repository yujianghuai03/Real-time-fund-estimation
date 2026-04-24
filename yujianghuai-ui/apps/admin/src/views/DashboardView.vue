<template>
  <section>
    <div class="stat-grid">
      <article class="stat">
        <span>租户</span>
        <strong>1</strong>
      </article>
      <article class="stat">
        <span>角色</span>
        <strong>2</strong>
      </article>
      <article class="stat">
        <span>权限节点</span>
        <strong>{{ totalPermissions }}</strong>
      </article>
      <article class="stat">
        <span>OAuth 客户端</span>
        <strong>1</strong>
      </article>
    </div>

    <div class="page-card">
      <el-timeline>
        <el-timeline-item timestamp="步骤 1" placement="top">
          初始化 `yujianghuai` 数据库，完成 RBAC 与菜单权限表。
        </el-timeline-item>
        <el-timeline-item timestamp="步骤 2" placement="top">
          配置 OAuth2 密码模式、JWT、JWK 和网关资源服务校验。
        </el-timeline-item>
        <el-timeline-item timestamp="步骤 3" placement="top">
          后台接入真实接口后，可从 `sys_menu` 动态生成路由与按钮权限。
        </el-timeline-item>
      </el-timeline>
    </div>
  </section>
</template>

<script setup lang="ts">
import { flattenMenus, permissionMenus } from '@yujianghuai/shared'

const totalPermissions = flattenMenus(permissionMenus).length
</script>
