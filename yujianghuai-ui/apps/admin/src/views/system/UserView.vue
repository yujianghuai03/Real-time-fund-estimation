<template>
  <section class="page-card">
    <div class="table-toolbar">
      <div>
        <h2>用户管理</h2>
        <p>维护租户下的登录账号、基础信息和角色授权。</p>
      </div>
      <el-space>
        <el-button type="primary" round @click="openCreate">新增用户</el-button>
        <el-button round :loading="loading" @click="loadData">刷新</el-button>
      </el-space>
    </div>

    <el-table :data="users" border v-loading="loading">
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="mobile" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="tenant" label="租户" width="100" />
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">{{ row.roles.join('、') || '未分配' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="640px">
      <el-form :model="form" label-width="92px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" required>
              <el-input v-model="form.username" :disabled="Boolean(editingId)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" required>
              <el-input v-model="form.nickname" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.mobile" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple clearable style="width: 100%" placeholder="请选择角色">
                <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createUser,
  deleteUser,
  listRoles,
  listUsers,
  updateUser,
  type RoleVO,
  type UserRequest,
  type UserVO
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const users = ref<UserVO[]>([])
const roles = ref<RoleVO[]>([])

const form = reactive<UserRequest>({
  username: '',
  password: '',
  nickname: '',
  realName: '',
  email: '',
  mobile: '',
  status: 1,
  roleIds: []
})

async function loadData() {
  loading.value = true
  try {
    const [userData, roleData] = await Promise.all([listUsers(), listRoles()])
    users.value = userData
    roles.value = roleData
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = undefined
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    realName: '',
    email: '',
    mobile: '',
    status: 1,
    roleIds: []
  })
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: UserVO) {
  resetForm()
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    realName: row.realName || '',
    email: row.email || '',
    mobile: row.mobile || '',
    status: row.status,
    roleIds: [...row.roleIds]
  })
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, form)
    } else {
      await createUser(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该用户吗？', '删除用户')
  await deleteUser(id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>
