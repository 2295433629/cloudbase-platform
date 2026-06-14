<template>
  <el-aside :width="collapse ? '64px' : '220px'" class="layout-aside">
    <!-- 顶部用户区域 -->
    <div class="user-section">
      <el-dropdown @command="handleCommand" trigger="click" :teleported="true">
        <div class="user-trigger">
          <div class="user-avatar">
            <el-icon :size="collapse ? 16 : 18"><User /></el-icon>
          </div>
          <template v-if="!collapse">
            <span class="user-name">{{ userStore.userInfo?.realName || '管理员' }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </template>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item command="changePassword">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapse"
      :collapse-transition="true"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <span style="font-size: 20px; color: #409EFF;">ClaudeBase</span>
      </el-menu-item>
      <template v-for="menu in visibleMenus" :key="menu.menuId">
        <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="String(menu.menuId)">
          <template #title>
            <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
            <span>{{ menu.menuName }}</span>
          </template>
          <el-menu-item
            v-for="child in menu.children"
            :key="child.menuId"
            :index="child.path"
          >
            <el-icon><component :is="getIcon(child.icon)" /></el-icon>
            <span>{{ child.menuName }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else-if="menu.path" :index="menu.path">
          <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
          <span>{{ menu.menuName }}</span>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- 底部折叠/展开按钮 -->
    <div class="sidebar-toggle" @click="$emit('toggleCollapse')">
      <el-icon :size="16"><component :is="collapse ? Expand : Fold" /></el-icon>
      <span v-show="!collapse">收起菜单</span>
    </div>

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
  </el-aside>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { resetDynamicRoutes } from '@/router'
import { getMenuTree } from '@/api/system'
import { getProfile, updateProfile, changePassword } from '@/api/auth'
import { Expand, Fold, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import * as icons from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { SysMenu } from '@/types/system'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

defineProps<{ collapse?: boolean }>()
defineEmits<{ toggleCollapse: [] }>()

// ==================== 菜单 ====================
const menuTree = ref<SysMenu[]>([])

onMounted(async () => {
  try {
    menuTree.value = await getMenuTree()
  } catch (e) {
    console.warn('加载菜单失败', e)
  }
})

const visibleMenus = computed(() => filterEnabled(menuTree.value))

function filterEnabled(menus: SysMenu[]): SysMenu[] {
  return menus
    .filter(m => m.status === 1)
    .map(m => ({
      ...m,
      children: m.children ? filterEnabled(m.children) : []
    }))
}

const defaultIcon = icons.Document
function getIcon(name: string) {
  return name && (icons as Record<string, any>)[name]
    ? (icons as Record<string, any>)[name]
    : defaultIcon
}

// ==================== 用户下拉 ====================
function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    resetDynamicRoutes()
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
  } catch { /* request拦截器已处理错误提示 */ }
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
    if (userStore.userInfo) {
      userStore.userInfo.realName = profileForm.realName
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }
    profileVisible.value = false
  } catch { /* request拦截器已处理错误提示 */ }
  profileLoading.value = false
}

// ==================== 修改密码 ====================
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (_rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const pwdRules: FormRules = {
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
    resetDynamicRoutes()
    router.push('/login')
  } catch { /* request拦截器已处理错误提示 */ }
  pwdLoading.value = false
}
</script>

<style scoped>
.layout-aside {
  background-color: #304156;
  overflow: hidden;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

/* 顶部用户区域 */
.user-section {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  overflow: hidden;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
  width: 100%;
  overflow: hidden;
}
.user-trigger:hover {
  background-color: rgba(255, 255, 255, 0.08);
}
.user-avatar {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.user-name {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.arrow-icon {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

/* 菜单 */
.layout-aside :deep(.el-menu) {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

/* 底部折叠按钮 */
.sidebar-toggle {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #bfcbd9;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  transition: all 0.3s;
  white-space: nowrap;
  overflow: hidden;
}
.sidebar-toggle:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.05);
}
</style>
