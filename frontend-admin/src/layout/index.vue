<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '240px'" class="aside">
      <div class="sidebar-brand">
        <div class="brand-icon" @click="toggleCollapse">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="6" fill="currentColor" fill-opacity="0.15"/>
            <path d="M7 18V10l3 3 2-2 3 3 4-4v8H7z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="11" cy="12" r="1.5" fill="currentColor"/>
          </svg>
        </div>
        <div class="brand-text" v-show="!isCollapsed">
          <span class="brand-name">ACAJ</span>
          <span class="brand-subtitle">智能服务平台</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/big-screen">
          <el-icon><Monitor /></el-icon>
          <span>校方大屏</span>
        </el-menu-item>

        <template v-if="isAdmin || isTeacher">
          <el-sub-menu index="/academic">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>学业管理</span>
            </template>
            <el-menu-item index="/academic/student">学生管理</el-menu-item>
            <el-menu-item index="/academic/warning">学业预警</el-menu-item>
            <el-menu-item index="/academic/study-plan">学习规划</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="isAdmin || isTeacher || isStudent">
          <el-sub-menu index="/employment">
            <template #title>
              <el-icon><Suitcase /></el-icon>
              <span>就业服务</span>
            </template>
            <el-menu-item index="/employment/resume">简历管理</el-menu-item>
            <el-menu-item v-if="isAdmin || isTeacher" index="/employment/job">岗位管理</el-menu-item>
            <el-menu-item v-if="isStudent" index="/employment/interview-simulator">AI 模拟面试</el-menu-item>
            <el-menu-item index="/employment/interview">面试记录</el-menu-item>
          </el-sub-menu>
        </template>

        <el-menu-item v-if="isStudent" index="/academic/study-plan">
          <el-icon><Tickets /></el-icon>
          <span>学习规划</span>
        </el-menu-item>

        <el-sub-menu v-if="isAdmin" index="/ai">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>AI 智能体</span>
          </template>
          <el-menu-item index="/ai/knowledge">知识库</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="isAdmin" index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="sidebar-footer" v-show="!isCollapsed">
        <div class="sidebar-collapse-btn" @click="toggleCollapse">
          <el-icon><Fold /></el-icon>
        </div>
      </div>
    </el-aside>

    <!-- Main Area -->
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <span class="collapse-trigger" @click="toggleCollapse" v-if="isCollapsed">
            <el-icon :size="18"><Expand /></el-icon>
          </span>
          <el-breadcrumb separator="">
            <el-breadcrumb-item :to="{ path: '/big-screen' }">
              <el-icon :size="16"><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">
              <span class="breadcrumb-separator">/</span>
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-avatar">
              <span class="avatar-circle">
                {{ userInitial }}
              </span>
              <span class="user-name" v-if="!isCollapsed">
                {{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}
              </span>
              <el-icon :size="14" class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <div class="user-info-dropdown">
                    <span class="user-info-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
                    <span class="user-info-role">{{ roleLabel }}</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const userType = computed(() => userStore.userInfo?.userType ?? 0)
const isAdmin = computed(() => userType.value === 2)
const isTeacher = computed(() => userType.value === 1)
const isStudent = computed(() => userType.value === 0)

const roleLabel = computed(() => {
  const labels: Record<number, string> = { 0: '学生', 1: '教师', 2: '管理员' }
  return labels[userType.value] || '未知'
})

const userInitial = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const isCollapsed = ref(false)

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

/* ------------------------------------------------------------------
   Sidebar
   ------------------------------------------------------------------ */
.aside {
  background: var(--color-sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* width transition via CSS grid is GPU-friendly; this is user-triggered, not continuous.
     Short duration + standard easing keeps the collapse responsive. */
  transition: width 180ms cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sidebar);
  position: relative;
  z-index: var(--z-sticky);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-4);
  min-height: 56px;
  border-bottom: 1px solid var(--color-sidebar-divider);
}

.brand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary-400);
  cursor: pointer;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.brand-icon:hover {
  background: rgba(255, 255, 255, 0.06);
}

.brand-text {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.brand-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-sidebar-logo-text);
  line-height: var(--line-height-tight);
  letter-spacing: 0.02em;
}

.brand-subtitle {
  font-size: var(--font-size-xs);
  color: var(--color-sidebar-text);
  line-height: 1.4;
}

/* Sidebar menu overrides */
.aside :deep(.el-menu) {
  border-right: none;
  background: transparent;
  padding: var(--space-3) 0;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.aside :deep(.el-menu-item) {
  margin: 2px var(--space-2);
  border-radius: var(--radius-md);
  height: 44px;
  line-height: 44px;
  font-size: var(--font-size-base);
  color: var(--color-sidebar-text);
  transition: all var(--transition-fast);
}

.aside :deep(.el-menu-item:hover) {
  background: var(--color-sidebar-bg-hover);
  color: var(--color-sidebar-text-hover);
}

.aside :deep(.el-menu-item.is-active) {
  background: var(--color-sidebar-bg-active);
  color: var(--color-primary-400);
  font-weight: var(--font-weight-semibold);
}

.aside :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--color-primary-400);
  border-radius: 0 2px 2px 0;
}

.aside :deep(.el-sub-menu__title) {
  margin: 2px var(--space-2);
  border-radius: var(--radius-md);
  height: 44px;
  line-height: 44px;
  font-size: var(--font-size-base);
  color: var(--color-sidebar-text);
  transition: all var(--transition-fast);
}

.aside :deep(.el-sub-menu__title:hover) {
  background: var(--color-sidebar-bg-hover);
  color: var(--color-sidebar-text-hover);
}

.aside :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--color-primary-400);
}

.aside :deep(.el-sub-menu .el-menu) {
  padding: var(--space-1) 0;
}

.aside :deep(.el-sub-menu .el-menu-item) {
  padding-left: 56px !important;
  height: 40px;
  line-height: 40px;
  font-size: var(--font-size-sm);
}

/* Collapsed state */
.aside :deep(.el-menu--collapse) {
  width: 64px;
}

.aside :deep(.el-menu--collapse .el-sub-menu__title) {
  padding: 0 !important;
  display: flex;
  justify-content: center;
}

.aside :deep(.el-menu--collapse .el-menu-item) {
  padding: 0 !important;
  display: flex;
  justify-content: center;
}

.sidebar-footer {
  border-top: 1px solid var(--color-sidebar-divider);
  padding: var(--space-3);
}

.sidebar-collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  border-radius: var(--radius-md);
  color: var(--color-sidebar-text);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.sidebar-collapse-btn:hover {
  background: var(--color-sidebar-bg-hover);
  color: var(--color-sidebar-text-hover);
}

/* ------------------------------------------------------------------
   Main Container
   ------------------------------------------------------------------ */
.main-container {
  flex-direction: column;
  overflow: hidden;
}

/* ------------------------------------------------------------------
   Header
   ------------------------------------------------------------------ */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  height: var(--header-height);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  z-index: var(--z-sticky);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.collapse-trigger {
  cursor: pointer;
  color: var(--color-text-secondary);
  padding: var(--space-2);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.collapse-trigger:hover {
  background: var(--color-border-light);
  color: var(--color-text-primary);
}

.breadcrumb-separator {
  margin: 0 var(--space-2);
  color: var(--color-text-placeholder);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-avatar:hover {
  background: var(--color-border-light);
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--color-primary-500);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.user-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.dropdown-arrow {
  color: var(--color-text-placeholder);
}

.user-info-dropdown {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-info-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.user-info-role {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

/* ------------------------------------------------------------------
   Main Content
   ------------------------------------------------------------------ */
.main {
  background: var(--color-body-bg);
  padding: var(--space-5);
  overflow-y: auto;
  height: calc(100vh - var(--header-height));
}
</style>
