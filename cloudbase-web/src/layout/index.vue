<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <h2>CloudBase</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/system/role">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/system/menu">
            <el-icon><Menu /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
          <el-menu-item index="/system/dict">
            <el-icon><Notebook /></el-icon>
            <span>字典管理</span>
          </el-menu-item>
          <el-menu-item index="/system/dept">
            <el-icon><OfficeBuilding /></el-icon>
            <span>部门管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="monitor">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>系统监控</span>
          </template>
          <el-menu-item index="/system/operlog">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/system/loginlog">
            <el-icon><Lock /></el-icon>
            <span>登录日志</span>
          </el-menu-item>
          <el-menu-item index="/system/online">
            <el-icon><Connection /></el-icon>
            <span>在线用户</span>
          </el-menu-item>
          <el-menu-item index="/system/monitor/server">
            <el-icon><Cpu /></el-icon>
            <span>服务监控</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="tools">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>系统工具</span>
          </template>
          <el-menu-item index="/system/config">
            <el-icon><SetUp /></el-icon>
            <span>参数配置</span>
          </el-menu-item>
          <el-menu-item index="/system/notice">
            <el-icon><Bell /></el-icon>
            <span>通知公告</span>
          </el-menu-item>
          <el-menu-item index="/system/job">
            <el-icon><Clock /></el-icon>
            <span>定时任务</span>
          </el-menu-item>
          <el-menu-item index="/system/gen">
            <el-icon><MagicStick /></el-icon>
            <span>代码生成</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              {{ userStore.userInfo?.realName || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <!-- 个人中心弹窗 -->
  <el-dialog v-model="profileVisible" title="个人中心" width="500px" destroy-on-close>
    <el-form :model="profileForm" label-width="80px">
      <el-form-item label="账号">
        <el-input :model-value="profileForm.account" disabled />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="profileForm.realName" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="profileForm.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="profileForm.email" />
      </el-form-item>
      <el-form-item label="头像URL">
        <el-input v-model="profileForm.avatar" />
      </el-form-item>
      <el-form-item label="状态">
        <el-tag :type="profileForm.status === 1 ? 'success' : 'danger'">
          {{ profileForm.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </el-form-item>
      <el-form-item label="最后登录">
        <span>{{ profileForm.lastLoginTime || '无' }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileVisible = false">取消</el-button>
      <el-button type="primary" :loading="profileLoading" @click="saveProfile">保存</el-button>
    </template>
  </el-dialog>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="pwdVisible" title="修改密码" width="420px" destroy-on-close>
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdVisible = false">取消</el-button>
      <el-button type="primary" :loading="pwdLoading" @click="savePassword">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, changePassword } from '@/api/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    openProfile()
  } else if (command === 'changePassword') {
    pwdVisible.value = true
  }
}

// ==================== 个人中心 ====================
const profileVisible = ref(false)
const profileLoading = ref(false)
const profileForm = reactive({
  account: '', realName: '', phone: '', email: '', avatar: '', status: 1, lastLoginTime: ''
})

async function openProfile() {
  try {
    const data = await getProfile()
    Object.assign(profileForm, {
      account: data.account || '',
      realName: data.realName || '',
      phone: data.phone || '',
      email: data.email || '',
      avatar: data.avatar || '',
      status: data.status,
      lastLoginTime: data.lastLoginTime || ''
    })
    profileVisible.value = true
  } catch (e) { /* request拦截器已处理错误提示 */ }
}

async function saveProfile() {
  profileLoading.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email,
      avatar: profileForm.avatar
    })
    ElMessage.success('个人信息已保存')
    // 同步更新header显示的用户名
    if (userStore.userInfo) {
      userStore.userInfo.realName = profileForm.realName
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }
    profileVisible.value = false
  } catch (e) { /* request拦截器已处理错误提示 */ }
  profileLoading.value = false
}

// ==================== 修改密码 ====================
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function savePassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdVisible.value = false
    userStore.logout()
    router.push('/login')
  } catch (e) { /* request拦截器已处理错误提示 */ }
  pwdLoading.value = false
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside { background-color: #304156; overflow: hidden; }
.layout-aside .logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.layout-aside .logo h2 { color: #fff; font-size: 20px; margin: 0; }
.layout-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}
.header-right .user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.layout-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
