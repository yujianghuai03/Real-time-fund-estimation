export type MenuType = 'CATALOG' | 'MENU' | 'BUTTON' | 'API'
export type MenuScope = 'ADMIN' | 'PORTAL'

export interface PermissionMenu {
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
  children?: PermissionMenu[]
}

export const permissionMenus: PermissionMenu[] = [
  {
    id: 1000,
    parentId: 0,
    type: 'CATALOG',
    scope: 'ADMIN',
    title: '系统管理',
    permission: 'system',
    path: '/system',
    component: 'Layout',
    icon: 'Setting',
    visible: true,
    sort: 1,
    children: [
      {
        id: 1100,
        parentId: 1000,
        type: 'MENU',
        scope: 'ADMIN',
        title: '用户管理',
        permission: 'system:user:view',
        path: '/system/user',
        component: 'system/user/index',
        icon: 'User',
        visible: true,
        sort: 1,
        children: [
          { id: 1101, parentId: 1100, type: 'BUTTON', scope: 'ADMIN', title: '用户新增', permission: 'system:user:add', visible: false, sort: 1 },
          { id: 1102, parentId: 1100, type: 'BUTTON', scope: 'ADMIN', title: '用户修改', permission: 'system:user:edit', visible: false, sort: 2 },
          { id: 1103, parentId: 1100, type: 'BUTTON', scope: 'ADMIN', title: '用户删除', permission: 'system:user:delete', visible: false, sort: 3 }
        ]
      },
      {
        id: 1200,
        parentId: 1000,
        type: 'MENU',
        scope: 'ADMIN',
        title: '角色管理',
        permission: 'system:role:view',
        path: '/system/role',
        component: 'system/role/index',
        icon: 'Avatar',
        visible: true,
        sort: 2,
        children: [
          { id: 1201, parentId: 1200, type: 'BUTTON', scope: 'ADMIN', title: '角色授权', permission: 'system:role:grant', visible: false, sort: 1 }
        ]
      },
      {
        id: 1300,
        parentId: 1000,
        type: 'MENU',
        scope: 'ADMIN',
        title: '菜单管理',
        permission: 'system:menu:view',
        path: '/system/menu',
        component: 'system/menu/index',
        icon: 'Menu',
        visible: true,
        sort: 3
      }
    ]
  },
  {
    id: 4000,
    parentId: 0,
    type: 'CATALOG',
    scope: 'PORTAL',
    title: '前台页面',
    permission: 'portal',
    path: '/',
    component: 'Layout',
    icon: 'House',
    visible: true,
    sort: 1,
    children: [
      {
        id: 4100,
        parentId: 4000,
        type: 'MENU',
        scope: 'PORTAL',
        title: '基金首页',
        permission: 'portal:home:view',
        path: '/',
        component: 'portal/home',
        icon: 'DataBoard',
        visible: true,
        sort: 1,
        children: [
          { id: 4101, parentId: 4100, type: 'BUTTON', scope: 'PORTAL', title: '基金搜索', permission: 'portal:fund:search', visible: false, sort: 1 },
          { id: 4102, parentId: 4100, type: 'BUTTON', scope: 'PORTAL', title: '自选添加', permission: 'portal:watchlist:add', visible: false, sort: 2 },
          { id: 4103, parentId: 4100, type: 'BUTTON', scope: 'PORTAL', title: '持仓修改', permission: 'portal:watchlist:edit', visible: false, sort: 3 },
          { id: 4104, parentId: 4100, type: 'BUTTON', scope: 'PORTAL', title: '自选删除', permission: 'portal:watchlist:delete', visible: false, sort: 4 },
          { id: 4105, parentId: 4100, type: 'BUTTON', scope: 'PORTAL', title: '反馈入口', permission: 'portal:feedback:view', visible: false, sort: 5 },
          { id: 4106, parentId: 4100, type: 'API', scope: 'PORTAL', title: '获取用户信息', permission: 'portal:userinfo:view', method: 'GET', apiPath: '/token/userinfo', visible: false, sort: 6 },
          { id: 4107, parentId: 4100, type: 'API', scope: 'PORTAL', title: '查看自选基金', permission: 'portal:watchlist:view', method: 'GET', apiPath: '/api/funds/watchlist', visible: false, sort: 7 }
        ]
      }
    ]
  }
]

export function flattenMenus(menus: PermissionMenu[]): PermissionMenu[] {
  return menus.flatMap((menu) => [menu, ...flattenMenus(menu.children ?? [])])
}
