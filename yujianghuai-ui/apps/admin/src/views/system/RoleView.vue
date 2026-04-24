<template>
  <section class="page-card">
    <div class="table-toolbar">
      <div>
        <h2>角色管理</h2>
        <p>维护角色基础信息，并分别配置前台、后台的菜单和功能权限。</p>
      </div>
      <el-space>
        <el-button type="primary" round @click="openCreate">新增角色</el-button>
        <el-button round :loading="loading" @click="loadData">刷新</el-button>
      </el-space>
    </div>

    <el-row :gutter="18">
      <el-col :xs="24" :lg="13">
        <el-table :data="roles" border highlight-current-row v-loading="loading" @current-change="selectRole">
          <el-table-column prop="roleCode" label="角色编码" min-width="130" />
          <el-table-column prop="roleName" label="角色名称" min-width="130" />
          <el-table-column label="数据权限" min-width="130">
            <template #default="{ row }">{{ dataScopeName(row.dataScope) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click.stop="remove(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :xs="24" :lg="11">
        <el-card shadow="never" class="auth-card">
          <template #header>
            <el-space style="justify-content: space-between; width: 100%">
              <span>{{ activeScope === 'ADMIN' ? '后台' : '前台' }}授权：{{ currentRole?.roleName ?? '请选择角色' }}</span>
              <el-button type="primary" size="small" round :disabled="!currentRole" @click="saveMenus">保存授权</el-button>
            </el-space>
          </template>
          <el-tabs v-model="activeScope" @tab-change="loadScopeMenus">
            <el-tab-pane label="后台权限" name="ADMIN" />
            <el-tab-pane label="前台权限" name="PORTAL" />
          </el-tabs>
          <el-tree
            ref="treeRef"
            :data="menus"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="{ label: 'title', children: 'children' }"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="560px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="角色编码" required>
          <el-input v-model="form.roleCode" placeholder="例如 ADMIN" />
        </el-form-item>
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" placeholder="例如 系统管理员" />
        </el-form-item>
        <el-form-item label="数据权限">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option :value="1" label="全部数据" />
            <el-option :value="2" label="本租户" />
            <el-option :value="3" label="本部门" />
            <el-option :value="4" label="本部门及以下" />
            <el-option :value="5" label="仅本人" />
            <el-option :value="6" label="自定义部门" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type ElTree } from 'element-plus'
import {
  createRole,
  deleteRole,
  listMenus,
  listRoles,
  roleMenuIds,
  saveRoleMenus,
  updateRole,
  type MenuVO,
  type RoleRequest,
  type RoleVO
} from '@/api/admin'
import type { MenuScope } from '@yujianghuai/shared'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const roles = ref<RoleVO[]>([])
const menus = ref<MenuVO[]>([])
const currentRole = ref<RoleVO>()
const treeRef = ref<InstanceType<typeof ElTree>>()
const activeScope = ref<MenuScope>('ADMIN')

const form = reactive<RoleRequest>({
  roleCode: '',
  roleName: '',
  dataScope: 5,
  sortOrder: 0,
  status: 1
})

async function loadData() {
  loading.value = true
  try {
    const roleData = await listRoles()
    roles.value = roleData
    currentRole.value = currentRole.value
      ? roleData.find((item) => item.id === currentRole.value?.id)
      : roleData[0]
    await loadScopeMenus()
  } finally {
    loading.value = false
  }
}

async function selectRole(role?: RoleVO) {
  if (!role) {
    return
  }
  currentRole.value = role
  await loadScopeMenus()
}

async function loadScopeMenus() {
  menus.value = await listMenus(activeScope.value)
  await nextTick()
  if (!currentRole.value) {
    treeRef.value?.setCheckedKeys([], false)
    return
  }
  const checkedKeys = await roleMenuIds(currentRole.value.id, activeScope.value)
  treeRef.value?.setCheckedKeys(checkedKeys, false)
}

function resetForm() {
  editingId.value = undefined
  Object.assign(form, {
    roleCode: '',
    roleName: '',
    dataScope: 5,
    sortOrder: 0,
    status: 1
  })
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: RoleVO) {
  resetForm()
  editingId.value = row.id
  Object.assign(form, {
    roleCode: row.roleCode,
    roleName: row.roleName,
    dataScope: row.dataScope,
    sortOrder: row.sortOrder,
    status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateRole(editingId.value, form)
    } else {
      await createRole(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该角色吗？', '删除角色')
  await deleteRole(id)
  ElMessage.success('删除成功')
  if (currentRole.value?.id === id) {
    currentRole.value = undefined
  }
  await loadData()
}

async function saveMenus() {
  if (!currentRole.value) {
    return
  }
  const checkedKeys = (treeRef.value?.getCheckedKeys(false) ?? []) as number[]
  const halfKeys = (treeRef.value?.getHalfCheckedKeys() ?? []) as number[]
  await saveRoleMenus(currentRole.value.id, activeScope.value, [...checkedKeys, ...halfKeys])
  ElMessage.success('授权已保存')
}

function dataScopeName(scope: number) {
  const map: Record<number, string> = {
    1: '全部数据',
    2: '本租户',
    3: '本部门',
    4: '本部门及以下',
    5: '仅本人',
    6: '自定义部门'
  }
  return map[scope] ?? '未知'
}

onMounted(loadData)
</script>
