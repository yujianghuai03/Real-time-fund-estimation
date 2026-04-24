<template>
  <main class="login-canvas">
    <section class="login-copy">
      <el-tag effect="dark" round>管理控制台</el-tag>
      <h1>面向管理员的权限控制台</h1>
      <p>围绕租户、用户、角色、菜单、按钮和 API 权限组织后台能力。</p>
    </section>
    <section class="login-card">
      <el-space direction="vertical" alignment="stretch" fill :size="20">
        <div>
          <h2>管理员登录</h2>
          <p style="color: #6d7b75">默认 admin / 123456，登录后调用后台权限接口。</p>
        </div>
        <el-form :model="form" label-position="top">
          <el-form-item label="租户">
            <el-input v-model="form.tenantId" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password @keyup.enter="submit" />
          </el-form-item>
          <el-button type="primary" size="large" round style="width: 100%" :loading="loading" @click="submit">
            进入后台
          </el-button>
        </el-form>
      </el-space>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  tenantId: '1',
  username: 'admin',
  password: '123456'
})

async function submit() {
  loading.value = true
  try {
    const token = await login(form.username, form.password, form.tenantId)
    localStorage.setItem('YJH_ADMIN_AUTH', 'true')
    localStorage.setItem('YJH_TOKEN', token.access_token)
    localStorage.setItem('YJH_TENANT_ID', form.tenantId)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
