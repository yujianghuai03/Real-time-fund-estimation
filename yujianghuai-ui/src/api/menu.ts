import { request } from '@/api/http'

export interface MenuTreeNode {
  id: number | string
  parentId?: number | string | null
  type?: string
  scope?: string
  title: string
  permission?: string
  path?: string
  component?: string
  icon?: string
  method?: string
  apiPath?: string
  visible?: boolean
  sort?: number
  status?: number
  children?: MenuTreeNode[]
}

export const fetchPortalMenus = (): Promise<MenuTreeNode[]> => {
  return request<MenuTreeNode[]>('/admin-api/menus?scope=PORTAL')
}
