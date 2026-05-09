import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'DataAnalysis' }
      },
      {
        path: 'academic/student',
        name: 'StudentList',
        component: () => import('@/views/academic/student/index.vue'),
        meta: { title: '学生管理', icon: 'User' }
      },
      {
        path: 'academic/warning',
        name: 'WarningList',
        component: () => import('@/views/academic/warning/index.vue'),
        meta: { title: '学业预警', icon: 'Warning' }
      },
      {
        path: 'academic/study-plan',
        name: 'StudyPlan',
        component: () => import('@/views/academic/study-plan/index.vue'),
        meta: { title: '学习规划', icon: 'Tickets' }
      },
      {
        path: 'academic/student/:id',
        name: 'StudentDetail',
        component: () => import('@/views/academic/student/detail.vue'),
        meta: { title: '学生详情', hidden: true }
      },
      {
        path: 'employment/resume',
        name: 'ResumeList',
        component: () => import('@/views/employment/resume/index.vue'),
        meta: { title: '简历管理', icon: 'Document' }
      },
      {
        path: 'employment/job',
        name: 'JobList',
        component: () => import('@/views/employment/job/index.vue'),
        meta: { title: '岗位管理', icon: 'Briefcase' }
      },
      {
        path: 'employment/interview',
        name: 'InterviewList',
        component: () => import('@/views/employment/interview/index.vue'),
        meta: { title: '面试记录', icon: 'ChatDotRound' }
      },
      {
        path: 'ai/knowledge',
        name: 'KnowledgeBase',
        component: () => import('@/views/ai/knowledge/index.vue'),
        meta: { title: '知识库', icon: 'Collection' }
      },
      {
        path: 'system/user',
        name: 'SysUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      },
      {
        path: 'system/role',
        name: 'SysRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'Avatar' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
