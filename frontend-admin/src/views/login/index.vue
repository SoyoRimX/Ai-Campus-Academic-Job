<template>
  <div class="login-page">
    <div class="login-split">
      <!-- Brand side -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-mark">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="currentColor" fill-opacity="0.12"/>
              <path d="M14 30V18l5 5 3.5-3.5L27 24.5 34 18v12H14z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="20" cy="21" r="2.5" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="brand-heading">ACAJ</h1>
          <p class="brand-desc">AI 校园学业就业智能服务平台</p>
          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>学业智能预警与管理</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>AI 简历生成与职位匹配</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>模拟面试与就业指导</span>
            </div>
          </div>
        </div>
        <div class="brand-pattern"></div>
      </div>

      <!-- Form side -->
      <div class="login-form-side">
        <div class="login-form-card">
          <div class="form-header">
            <h2>登录管理后台</h2>
            <p>使用您的账号登录系统</p>
          </div>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="用户名"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item class="submit-item">
              <el-button
                type="primary"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="form-footer">
            <span class="demo-hint">演示账号：admin / admin123</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  background: var(--color-body-bg);
}

.login-split {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

/* ------------------------------------------------------------------
   Brand Side
   ------------------------------------------------------------------ */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, var(--color-primary-700) 0%, var(--color-primary-900) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  min-width: 360px;
}

.brand-content {
  position: relative;
  z-index: 2;
  color: #fff;
  padding: var(--space-10);
  max-width: 420px;
}

.brand-mark {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: var(--space-6);
}

.brand-heading {
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: var(--font-weight-bold);
  color: #fff;
  margin-bottom: var(--space-3);
  letter-spacing: 0.04em;
}

.brand-desc {
  font-size: var(--font-size-lg);
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: var(--space-10);
  line-height: var(--line-height-relaxed);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--font-size-base);
  color: rgba(255, 255, 255, 0.8);
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-primary-300);
  flex-shrink: 0;
}

.brand-pattern {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.04) 0%, transparent 50%);
  pointer-events: none;
}

/* ------------------------------------------------------------------
   Form Side
   ------------------------------------------------------------------ */
.login-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  min-width: 360px;
}

.login-form-card {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: var(--space-8);
}

.form-header h2 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.form-header p {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.submit-item :deep(.el-form-item__content) {
  margin-top: var(--space-2);
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.05em;
}

.form-footer {
  text-align: center;
  margin-top: var(--space-6);
}

.demo-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-placeholder);
}

/* ------------------------------------------------------------------
   Responsive
   ------------------------------------------------------------------ */
@media (max-width: 768px) {
  .login-brand {
    display: none;
  }

  .login-form-side {
    flex: 1;
    padding: var(--space-5);
  }
}
</style>
