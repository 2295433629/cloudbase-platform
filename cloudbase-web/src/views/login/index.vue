<template>
  <div class="login-container">
    <!-- 装饰背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <h2 class="login-title">CloudBase 基础平台</h2>
        <p class="login-subtitle">企业级管理系统</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="handleLogin" @submit.prevent>
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入账号" prefix-icon="User" clearable />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                    show-password>
            <template #suffix>
              <el-tooltip :content="passwordStrength.text" placement="top" :disabled="passwordStrength.level === 0">
                <div class="pwd-strength-bar" :style="{ width: passwordStrength.width }"
                     :class="'strength-' + passwordStrength.type"></div>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="captcha">
          <el-row :gutter="10" style="width:100%">
            <el-col :span="14">
              <el-input v-model="form.captcha" placeholder="验证码" prefix-icon="Key" clearable />
            </el-col>
            <el-col :span="10">
              <img :src="captchaImage" @click="refreshCaptcha" class="captcha-img" alt="验证码"
                   title="点击刷新" />
            </el-col>
          </el-row>
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </div>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%; height:42px; font-size:16px">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {getCaptcha} from '@/api/auth'
import {ElMessage} from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const captchaImage = ref('')
const captchaUuid = ref('')
const savedAccount = localStorage.getItem('rememberAccount')
const rememberMe = ref(!!savedAccount)

const form = reactive({
  account: savedAccount || 'admin',
  password: '',
  captcha: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 密码强度检测
const passwordStrength = reactive({ level: 0, text: '', type: '', width: '0%' })

watch(() => form.password, (val) => {
  if (!val) {
    passwordStrength.level = 0
    passwordStrength.text = ''
    passwordStrength.type = ''
    passwordStrength.width = '0%'
    return
  }
  if (val.length < 6) {
    Object.assign(passwordStrength, { level: 1, text: '弱', type: 'weak', width: '33%' })
  } else if (val.length < 10 && !/[A-Z]/.test(val)) {
    Object.assign(passwordStrength, { level: 2, text: '中', type: 'medium', width: '66%' })
  } else {
    Object.assign(passwordStrength, { level: 3, text: '强', type: 'strong', width: '100%' })
  }
})

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.image
    captchaUuid.value = res.uuid
  } catch {
    ElMessage.error('获取验证码失败')
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.account, form.password, captchaUuid.value, form.captcha)
    // 记住我：保存账号到 localStorage；未勾选则清除
    if (rememberMe.value) {
      localStorage.setItem('rememberAccount', form.account)
    } else {
      localStorage.removeItem('rememberAccount')
    }
    ElMessage.success('登录成功')
    await router.push('/dashboard')
  } catch {
    refreshCaptcha()
    form.captcha = ''
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.06);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -120px;
  right: -80px;
  background: rgba(64, 158, 255, 0.08);
}

.circle-2 {
  width: 250px;
  height: 250px;
  bottom: -80px;
  left: -60px;
  background: rgba(103, 194, 58, 0.06);
}

.circle-3 {
  width: 180px;
  height: 180px;
  top: 35%;
  left: 8%;
  background: rgba(230, 162, 60, 0.05);
}

.login-card {
  width: 420px;
  padding: 16px 12px;
  z-index: 1;
  border-radius: 16px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3) !important;
}

:root.dark .login-card {
  --el-bg-color: rgba(26, 26, 26, 0.95);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

:root.dark .login-title {
  color: var(--text-color);
}

.login-subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.captcha-img {
  width: 100%;
  height: 40px;
  cursor: pointer;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 10px;
}

/* 密码强度条 */
.pwd-strength-bar {
  height: 3px;
  border-radius: 2px;
  margin-top: 4px;
  transition: all 0.3s;
}

.strength-weak { background: #f56c6c; }
.strength-medium { background: #e6a23c; }
.strength-strong { background: #67c23a; }
</style>
