<template>
  <section class="page-card">
    <div class="table-toolbar">
      <div>
        <h2>菜单权限</h2>
        <p>在后台统一维护前台、后台的菜单权限和功能权限节点。</p>
      </div>
      <el-space wrap>
        <el-button type="primary" round @click="openCreate(0, 'CATALOG')">新增目录</el-button>
        <el-button round @click="openCreate(0, 'MENU')">新增菜单</el-button>
        <el-button round @click="openCreate(0, 'BUTTON')">新增按钮</el-button>
        <el-button round @click="openCreate(0, 'API')">新增 API</el-button>
        <el-button round :loading="loading" @click="loadMenus">刷新</el-button>
      </el-space>
    </div>

    <el-tabs v-model="activeScope" @tab-change="loadMenus">
      <el-tab-pane label="后台权限" name="ADMIN" />
      <el-tab-pane label="前台权限" name="PORTAL" />
    </el-tabs>

    <el-table
      :data="menus"
      row-key="id"
      border
      default-expand-all
      v-loading="loading"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="title" label="名称" min-width="180" />
      <el-table-column prop="type" label="类型" width="110">
        <template #default="{ row }">
          <el-tag :type="tagType(row.type)" effect="dark">{{ typeName(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="permission" label="权限标识" min-width="190" />
      <el-table-column prop="path" label="路由" min-width="160" />
      <el-table-column prop="apiPath" label="API 路径" min-width="170" />
      <el-table-column prop="method" label="方法" width="90" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="显示" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.visible" disabled />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openCreate(row.id, 'MENU')">新增子级</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑权限节点' : '新增权限节点'" width="680px">
      <el-form :model="form" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级节点">
              <el-tree-select
                v-model="form.parentId"
                :data="parentOptions"
                check-strictly
                :render-after-expand="false"
                style="width: 100%"
                placeholder="请选择上级节点"
                :props="{ label: 'title', children: 'children', value: 'id' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点类型" required>
              <el-select v-model="form.menuType" style="width: 100%">
                <el-option label="目录" value="CATALOG" />
                <el-option label="菜单" value="MENU" />
                <el-option label="按钮" value="BUTTON" />
                <el-option label="API 权限" value="API" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限归属" required>
              <el-select v-model="form.menuScope" style="width: 100%">
                <el-option label="后台" value="ADMIN" />
                <el-option label="前台" value="PORTAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="form.menuName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限标识">
              <el-input v-model="form.permission" placeholder="system:user:add" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由路径">
              <el-input v-model="form.path" placeholder="/system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径">
              <el-input v-model="form.component" placeholder="system/user/index" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标">
              <el-input v-model="form.icon" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求方法">
              <el-select v-model="form.method" clearable style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="API 路径">
              <el-input v-model="form.apiPath" placeholder="/admin-api/users" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否显示">
              <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" active-text="显示" inactive-text="隐藏" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createMenu, deleteMenu, listMenus, updateMenu, type MenuRequest, type MenuVO } from '@/api/admin'
import type { MenuScope, MenuType } from '@yujianghuai/shared'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const menus = ref<MenuVO[]>([])
const activeScope = ref<MenuScope>('ADMIN')

const form = reactive<MenuRequest>({
  parentId: 0,
  menuType: 'MENU',
  menuScope: 'ADMIN',
  menuName: '',
  permission: '',
  path: '',
  component: '',
  icon: '',
  method: '',
  apiPath: '',
  sortOrder: 0,
  visible: 1,
  status: 1
})

const parentOptions = computed(() => [{ id: 0, title: '根节点', children: menus.value }])

async function loadMenus() {
  loading.value = true
  try {
    menus.value = await listMenus(activeScope.value)
  } finally {
    loading.value = false
  }
}

function resetForm(parentId = 0, type: MenuType = 'MENU', scope: MenuScope = activeScope.value) {
  editingId.value = undefined
  Object.assign(form, {
    parentId,
    menuType: type,
    menuScope: scope,
    menuName: '',
    permission: '',
    path: '',
    component: '',
    icon: '',
    method: '',
    apiPath: '',
    sortOrder: 0,
    visible: 1,
    status: 1
  })
}

function openCreate(parentId = 0, type: MenuType = 'MENU') {
  resetForm(parentId, type, activeScope.value)
  dialogVisible.value = true
}

function openEdit(row: MenuVO) {
  resetForm(row.parentId || 0, row.type, row.scope)
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId || 0,
    menuType: row.type,
    menuScope: row.scope,
    menuName: row.title,
    permission: row.permission || '',
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    method: row.method || '',
    apiPath: row.apiPath || '',
    sortOrder: row.sort || 0,
    visible: row.visible ? 1 : 0,
    status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateMenu(editingId.value, form)
    } else {
      await createMenu(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadMenus()
  } finally {
    saving.value = false
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该菜单或权限节点吗？', '删除菜单')
  await deleteMenu(id)
  ElMessage.success('删除成功')
  await loadMenus()
}

function tagType(type: MenuType) {
  const map = {
    CATALOG: 'info',
    MENU: 'success',
    BUTTON: 'warning',
    API: 'danger'
  } as const
  return map[type]
}

function typeName(type: MenuType) {
  const map: Record<MenuType, string> = {
    CATALOG: '目录',
    MENU: '菜单',
    BUTTON: '按钮',
    API: '接口'
  }
  return map[type]
}

onMounted(loadMenus)
</script>
