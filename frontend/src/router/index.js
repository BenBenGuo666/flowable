import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/leave',
    name: 'Leave',
    redirect: '/leave/apply',
    children: [
      {
        path: 'apply',
        name: 'LeaveApply',
        component: () => import('../views/leave/LeaveApply.vue'),
        meta: { title: '请假申请' }
      },
      {
        path: 'list',
        name: 'LeaveList',
        component: () => import('../views/leave/LeaveList.vue'),
        meta: { title: '我的请假' }
      },
      {
        path: 'pending',
        name: 'LeavePending',
        component: () => import('../views/leave/LeavePending.vue'),
        meta: { title: '待审批' }
      },
      {
        path: 'detail/:id',
        name: 'LeaveDetail',
        component: () => import('../views/leave/LeaveDetail.vue'),
        meta: { title: '请假详情' }
      }
    ]
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '工作台' }
  },
  {
    path: '/process',
    name: 'Process',
    redirect: '/process/designer',
    children: [
      {
        path: 'designer',
        name: 'ProcessDesigner',
        component: () => import('../views/process/ProcessDesigner.vue'),
        meta: { title: '流程设计器' }
      },
      {
        path: 'definitions',
        name: 'ProcessDefinitions',
        component: () => import('../views/process/ProcessDefinitions.vue'),
        meta: { title: '流程定义' }
      },
      {
        path: 'instances',
        name: 'ProcessInstances',
        component: () => import('../views/process/ProcessInstances.vue'),
        meta: { title: '流程实例' }
      },
      {
        path: 'templates',
        name: 'ProcessTemplates',
        component: () => import('../views/process/ProcessTemplates.vue'),
        meta: { title: '流程模板' }
      }
    ]
  },
  {
    path: '/task',
    name: 'Task',
    redirect: '/task/my-tasks',
    children: [
      {
        path: 'my-tasks',
        name: 'MyTasks',
        component: () => import('../views/task/MyTasks.vue'),
        meta: { title: '我的任务' }
      }
    ]
  },
  {
    path: '/identity',
    name: 'Identity',
    redirect: '/identity/users',
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/identity/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('../views/identity/RoleManagement.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: () => import('../views/identity/PermissionManagement.vue'),
        meta: { title: '权限管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 设置页面标题和登录验证
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Flowable 工作流`
  }

  // 登录验证
  const token = localStorage.getItem('token')
  const isLoginPage = to.path === '/login'

  if (!token && !to.meta.noAuth && !isLoginPage) {
    // 未登录且不是免登录页面，跳转到登录页
    next('/login')
  } else if (token && isLoginPage) {
    // 已登录访问登录页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router
