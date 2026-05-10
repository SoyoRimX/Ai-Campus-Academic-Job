<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span>🎓 ACAJ 管理平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
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
            <el-menu-item index="/employment/interview-simulator">AI 模拟面试</el-menu-item>
            <el-menu-item index="/employment/interview">面试记录</el-menu-item>
          </el-sub-menu>
        </template>

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
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const userType = computed(() => userStore.userInfo?.userType ?? 0)
const isAdmin = computed(() => userType.value === 2)
const isTeacher = computed(() => userType.value === 1)
const isStudent = computed(() => userType.value === 0)

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background-color: #304156; overflow-y: auto; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 16px; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,0.1); }
.header { background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; }
.header-right { cursor: pointer; }
.main { background: #f0f2f5; padding: 20px; }
</style>
