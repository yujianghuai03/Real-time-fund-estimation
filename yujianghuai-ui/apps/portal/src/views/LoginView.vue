<template>
  <main class="portal-shell">
    <header class="portal-nav">
      <router-link class="brand" to="/">
        <span class="brand-mark">Y</span>
        <span>基金实时预估V1.0</span>
      </router-link>
    </header>

    <section class="login-panel">
      <el-space direction="vertical" alignment="stretch" fill :size="20">
        <div>
          <el-tag round>基金实时预估V1.0</el-tag>
          <h2>登录后查看自选基金</h2>
          <p>租户从数据库实时加载，默认优先使用你上次在当前浏览器选择的租户。</p>
        </div>

        <el-alert
          v-if="tenantLoadError"
          :title="tenantLoadError"
          type="warning"
          show-icon
          :closable="false"
        />

        <el-tabs v-model="activeTab" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" label-position="top">
              <el-form-item label="租户">
                <el-select
                  v-model="loginForm.tenantId"
                  placeholder="请选择租户"
                  style="width: 100%"
                  filterable
                  :loading="tenantLoading"
                >
                  <el-option
                    v-for="item in tenantOptions"
                    :key="item.id"
                    :label="item.tenantName"
                    :value="String(item.id)"
                  >
                    <div class="tenant-option">
                      <span>{{ item.tenantName }}</span>
                      <span class="tenant-option-code">{{ item.tenantCode }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="loginForm.username" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  show-password
                  @keyup.enter="submitLogin"
                />
              </el-form-item>
              <el-button
                type="primary"
                size="large"
                round
                style="width: 100%"
                :loading="loginLoading"
                @click="submitLogin"
              >
                登录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" label-position="top">
              <el-form-item label="租户">
                <el-select
                  v-model="registerForm.tenantId"
                  placeholder="请选择租户"
                  style="width: 100%"
                  filterable
                  :loading="tenantLoading"
                >
                  <el-option
                    v-for="item in tenantOptions"
                    :key="item.id"
                    :label="item.tenantName"
                    :value="item.id"
                  >
                    <div class="tenant-option">
                      <span>{{ item.tenantName }}</span>
                      <span class="tenant-option-code">{{ item.tenantCode }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="registerForm.username" />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="registerForm.nickname" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="registerForm.password" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  show-password
                  @keyup.enter="submitRegister"
                />
              </el-form-item>
              <el-button
                type="primary"
                size="large"
                round
                style="width: 100%"
                :loading="registerLoading"
                @click="submitRegister"
              >
                注册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-space>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTenants, login, register, type TenantOption } from '../api/auth'

const TENANT_ID_KEY = 'YJH_TENANT_ID'
const TENANT_NAME_KEY = 'YJH_TENANT_NAME'

const router = useRouter()
const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const tenantLoading = ref(false)
const tenantLoadError = ref('')
const tenantOptions = ref<TenantOption[]>([])

const loginForm = reactive({
  tenantId: '',
  username: 'admin',
  password: '123456'
})

const registerForm = reactive({
  tenantId: 0,
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

onMounted(() => {
  void loadTenants()
})

async function loadTenants() {
  tenantLoading.value = true
  tenantLoadError.value = ''
  try {
    const tenants = await getTenants()
    tenantOptions.value = tenants

    if (tenants.length > 0) {
      const cachedTenantId = localStorage.getItem(TENANT_ID_KEY) ?? ''
      const matchedTenant = tenants.find((item) => String(item.id) === cachedTenantId)
      const defaultTenant = matchedTenant ?? tenants[0]
      loginForm.tenantId = String(defaultTenant.id)
      registerForm.tenantId = defaultTenant.id
    }
  } catch (error) {
    tenantLoadError.value = error instanceof Error ? error.message : '租户列表加载失败'
  } finally {
    tenantLoading.value = false
  }
}

function getTenantNameById(tenantId: string | number) {
  return tenantOptions.value.find((item) => String(item.id) === String(tenantId))?.tenantName ?? ''
}

async function submitLogin() {
  if (!loginForm.tenantId) {
    ElMessage.warning('请选择租户')
    return
  }

  loginLoading.value = true
  try {
    const token = await login(loginForm.username, loginForm.password, loginForm.tenantId)
    localStorage.setItem('YJH_TOKEN', token.access_token)
    localStorage.setItem(TENANT_ID_KEY, loginForm.tenantId)
    localStorage.setItem(TENANT_NAME_KEY, getTenantNameById(loginForm.tenantId))
    localStorage.setItem('YJH_USERNAME', loginForm.username)
    ElMessage.success('登录成功')
    router.replace('/')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loginLoading.value = false
  }
}

async function submitRegister() {
  if (!registerForm.tenantId) {
    ElMessage.warning('请选择租户')
    return
  }
  if (!registerForm.username.trim() || !registerForm.nickname.trim() || !registerForm.password) {
    ElMessage.warning('请完整填写注册信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  registerLoading.value = true
  try {
    const result = await register({
      tenantId: registerForm.tenantId,
      username: registerForm.username.trim(),
      nickname: registerForm.nickname.trim(),
      password: registerForm.password
    })

    loginForm.tenantId = String(registerForm.tenantId)
    loginForm.username = registerForm.username.trim()
    loginForm.password = registerForm.password
    localStorage.setItem(TENANT_ID_KEY, loginForm.tenantId)
    localStorage.setItem(TENANT_NAME_KEY, getTenantNameById(loginForm.tenantId))

    registerForm.username = ''
    registerForm.nickname = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    activeTab.value = 'login'

    ElMessage.success(`注册成功，当前租户：${result.tenantName}`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.tenant-option {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.tenant-option-code {
  color: var(--el-text-color-secondary);
}
</style>
