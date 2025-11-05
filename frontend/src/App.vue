<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-layout class="app-layout">
            <!-- 顶部导航栏 -->
            <n-layout-header class="app-header glass-effect" bordered>
              <div class="header-content">
                <div class="logo">
                  <h2>🌊 Flowable 工作流</h2>
                </div>
                <n-menu
                  v-model:value="activeKey"
                  mode="horizontal"
                  :options="menuOptions"
                  @update:value="handleMenuUpdate"
                />
              </div>
            </n-layout-header>

            <!-- 主内容区 -->
            <n-layout has-sider class="main-layout">
              <!-- 侧边栏 -->
              <n-layout-sider
                bordered
                collapse-mode="width"
                :collapsed-width="64"
                :width="240"
                show-trigger
                class="app-sider"
              >
                <n-menu
                  v-model:value="activeKey"
                  :collapsed-width="64"
                  :collapsed-icon-size="22"
                  :options="sideMenuOptions"
                  @update:value="handleMenuUpdate"
                />
              </n-layout-sider>

              <!-- 内容区域 -->
              <n-layout-content class="app-content">
                <div class="content-wrapper">
                  <router-view v-slot="{ Component }">
                    <transition name="fade" mode="out-in">
                      <component :is="Component" />
                    </transition>
                  </router-view>
                </div>
              </n-layout-content>
            </n-layout>
          </n-layout>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { appleTheme } from './config/theme'

const router = useRouter()
const route = useRoute()
const activeKey = ref('home')

// 主题覆盖配置
const themeOverrides = appleTheme

// 顶部菜单选项
const menuOptions = [
  {
    label: '首页',
    key: 'home',
    path: '/'
  },
  {
    label: '工作台',
    key: 'dashboard',
    path: '/dashboard'
  }
]

// 侧边栏菜单选项
const sideMenuOptions = [
  {
    label: '请假管理',
    key: 'leave',
    children: [
      {
        label: '发起请假',
        key: 'leave-apply',
        path: '/leave/apply'
      },
      {
        label: '我的请假',
        key: 'leave-list',
        path: '/leave/list'
      },
      {
        label: '待我审批',
        key: 'leave-pending',
        path: '/leave/pending'
      }
    ]
  },
  {
    label: '任务中心',
    key: 'task',
    children: [
      {
        label: '我的任务',
        key: 'my-tasks',
        path: '/task/my-tasks'
      }
    ]
  },
  {
    label: '流程管理',
    key: 'process',
    children: [
      {
        label: '流程设计器',
        key: 'process-designer',
        path: '/process/designer'
      },
      {
        label: '流程定义',
        key: 'process-definitions',
        path: '/process/definitions'
      },
      {
        label: '流程实例',
        key: 'process-instances',
        path: '/process/instances'
      },
      {
        label: '流程模板',
        key: 'process-templates',
        path: '/process/templates'
      }
    ]
  }
]

// 根据路由路径找到对应的key
const getKeyFromPath = (path) => {
  const findKey = (options) => {
    for (const option of options) {
      if (option.path === path) {
        return option.key
      }
      if (option.children) {
        const key = findKey(option.children)
        if (key) return key
      }
    }
    return null
  }
  return findKey([...menuOptions, ...sideMenuOptions]) || 'home'
}

// 监听路由变化，同步activeKey
watch(() => route.path, (newPath) => {
  activeKey.value = getKeyFromPath(newPath)
}, { immediate: true })

// 处理菜单更新
const handleMenuUpdate = (key, item) => {
  activeKey.value = key
  const findPath = (options) => {
    for (const option of options) {
      if (option.key === key && option.path) {
        return option.path
      }
      if (option.children) {
        const path = findPath(option.children)
        if (path) return path
      }
    }
    return null
  }

  const path = findPath([...menuOptions, ...sideMenuOptions])
  if (path) {
    router.push(path)
  }
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.app-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #007AFF;
}

.main-layout {
  height: calc(100vh - 64px);
}

.app-sider {
  background: #FFFFFF;
  height: 100%;
}

.app-content {
  background: #F2F2F7;
  height: 100%;
  overflow: auto;
}

.content-wrapper {
  padding: 24px;
  min-height: 100%;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .app-sider {
    background: #1C1C1E;
  }

  .app-content {
    background: #000000;
  }
}
</style>
