import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: '/system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: '/system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: '/system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: '/system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Notebook' }
      },
      {
        path: '/system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: '/system/operlog',
        name: 'SystemOperLog',
        component: () => import('@/views/system/operlog/index.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: '/system/loginlog',
        name: 'SystemLoginLog',
        component: () => import('@/views/system/loginlog/index.vue'),
        meta: { title: '登录日志', icon: 'Lock' }
      },
      {
        path: '/system/online',
        name: 'SystemOnline',
        component: () => import('@/views/system/online/index.vue'),
        meta: { title: '在线用户', icon: 'Connection' }
      },
      {
        path: '/system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '参数配置', icon: 'Tools' }
      },
      {
        path: '/system/notice',
        name: 'SystemNotice',
        component: () => import('@/views/system/notice/index.vue'),
        meta: { title: '通知公告', icon: 'Bell' }
      },
      {
        path: '/system/monitor/server',
        name: 'MonitorServer',
        component: () => import('@/views/system/monitor/server/index.vue'),
        meta: { title: '服务监控', icon: 'Monitor' }
      },
      {
        path: '/system/job',
        name: 'SystemJob',
        component: () => import('@/views/system/job/index.vue'),
        meta: { title: '定时任务', icon: 'Clock' }
      },
      {
        path: '/system/gen',
        name: 'SystemGen',
        component: () => import('@/views/system/gen/index.vue'),
        meta: { title: '代码生成', icon: 'Magic' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
