import { request, type MenuScope, type MenuType } from '@yujianghuai/shared'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface UserVO {
  id: number
  username: string
  nickname: string
  realName?: string
  email?: string
  mobile?: string
  tenant: string
  status: number
  roleIds: number[]
  roles: string[]
}

export interface UserRequest {
  deptId?: number
  username: string
  password?: string
  nickname: string
  realName?: string
  email?: string
  mobile?: string
  status: number
  roleIds: number[]
}

export interface RoleVO {
  id: number
  roleCode: string
  roleName: string
  dataScope: number
  sortOrder: number
  status: number
}

export interface RoleRequest {
  roleCode: string
  roleName: string
  dataScope: number
  sortOrder: number
  status: number
}

export interface MenuVO {
  id: number
  parentId: number
  type: MenuType
  scope: MenuScope
  title: string
  permission: string
  path?: string
  component?: string
  icon?: string
  method?: string
  apiPath?: string
  visible: boolean
  sort: number
  status: number
  children?: MenuVO[]
}

export interface MenuRequest {
  parentId: number
  menuType: MenuType
  menuScope: MenuScope
  menuName: string
  permission?: string
  path?: string
  component?: string
  icon?: string
  method?: string
  apiPath?: string
  sortOrder: number
  visible: number
  status: number
}

export async function login(username: string, password: string, tenantId: string) {
  const params = new URLSearchParams()
  params.set('grant_type', 'password')
  params.set('username', username)
  params.set('password', password)
  params.set('scope', 'openid profile api.read api.write')
  params.set('TENANT-ID', tenantId)
  params.set('LOGIN-TYPE', 'ADMIN')
  const basic = btoa('yujianghuai-client:yujianghuai-secret')
  const response = await request.post('/oauth2/token', params, {
    headers: {
      Authorization: `Basic ${basic}`,
      'Content-Type': 'application/x-www-form-urlencoded',
      'TENANT-ID': tenantId,
      'LOGIN-TYPE': 'ADMIN'
    }
  })
  return response.data
}

export async function listUsers() {
  const response = await request.get<ApiResult<UserVO[]>>('/admin-api/users')
  return response.data.data
}

export async function createUser(data: UserRequest) {
  const response = await request.post<ApiResult<UserVO>>('/admin-api/users', data)
  return response.data.data
}

export async function updateUser(id: number, data: UserRequest) {
  const response = await request.put<ApiResult<UserVO>>(`/admin-api/users/${id}`, data)
  return response.data.data
}

export async function deleteUser(id: number) {
  await request.delete(`/admin-api/users/${id}`)
}

export async function listRoles() {
  const response = await request.get<ApiResult<RoleVO[]>>('/admin-api/roles')
  return response.data.data
}

export async function createRole(data: RoleRequest) {
  const response = await request.post<ApiResult<RoleVO>>('/admin-api/roles', data)
  return response.data.data
}

export async function updateRole(id: number, data: RoleRequest) {
  const response = await request.put<ApiResult<RoleVO>>(`/admin-api/roles/${id}`, data)
  return response.data.data
}

export async function deleteRole(id: number) {
  await request.delete(`/admin-api/roles/${id}`)
}

export async function roleMenuIds(roleId: number, scope: MenuScope) {
  const response = await request.get<ApiResult<number[]>>(`/admin-api/roles/${roleId}/menus`, {
    params: { scope }
  })
  return response.data.data
}

export async function saveRoleMenus(roleId: number, scope: MenuScope, menuIds: number[]) {
  await request.put(`/admin-api/roles/${roleId}/menus`, { menuIds }, {
    params: { scope }
  })
}

export async function listMenus(scope: MenuScope) {
  const response = await request.get<ApiResult<MenuVO[]>>('/admin-api/menus', {
    params: { scope }
  })
  return response.data.data
}

export async function createMenu(data: MenuRequest) {
  const response = await request.post<ApiResult<unknown>>('/admin-api/menus', data)
  return response.data.data
}

export async function updateMenu(id: number, data: MenuRequest) {
  const response = await request.put<ApiResult<unknown>>(`/admin-api/menus/${id}`, data)
  return response.data.data
}

export async function deleteMenu(id: number) {
  await request.delete(`/admin-api/menus/${id}`)
}
