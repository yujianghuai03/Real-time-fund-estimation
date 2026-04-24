import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from './layouts/AdminLayout.vue'
import DashboardView from './views/DashboardView.vue'
import LoginView from './views/LoginView.vue'
import MenuPermissionView from './views/system/MenuPermissionView.vue'
import RoleView from './views/system/RoleView.vue'
import UserView from './views/system/UserView.vue'

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: DashboardView },
        { path: 'system/user', name: 'user', component: UserView },
        { path: 'system/role', name: 'role', component: RoleView },
        { path: 'system/menu', name: 'menu', component: MenuPermissionView }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.name !== 'login' && !localStorage.getItem('YJH_ADMIN_AUTH')) {
    return { name: 'login' }
  }
  return true
})

export default router
