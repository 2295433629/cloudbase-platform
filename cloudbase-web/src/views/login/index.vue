<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">CloudBase 基础平台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                    @keyup.enter="handleLogin" show-password />
        </el-form-item>
        <el-form-item prop="captcha">
          <el-row :gutter="10" style="width:100%">
            <el-col :span="14">
              <el-input v-model="form.captcha" placeholder="验证码" prefix-icon="Key" />
            </el-col>
            <el-col :span="10">
              <img :src="captchaImage" @click="refreshCaptcha" class="captcha-img" alt="验证码"
                   title="点击刷新" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaUuid = ref('')

const form = reactive({
  account: 'admin',
  password: '',
  captcha: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.image
    captchaUuid.value = res.uuid
  } catch (e) {
    ElMessage.error('获取验证码失败')
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.account, form.password, captchaUuid.value, form.captcha)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    refreshCaptcha()
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 10px;
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.captcha-img {
  width: 100%;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
</style>
