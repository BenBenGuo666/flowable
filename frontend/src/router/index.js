import { createRouter, createWebHistory } from 'vue-router'

const routes = [
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - Flowable 工作流`
  }
  next()
})

export default router
