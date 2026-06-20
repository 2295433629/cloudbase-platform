import request from '@/utils/request'
import type {PageParams, TableResponse} from '@/types/api'
import type {SysDept, SysDict, SysMenu, SysPost, SysRole, SysUser} from '@/types/system'

// 用户管理
export function getUserPage(data: PageParams & Partial<SysUser>): Promise<TableResponse<SysUser>> { return request({ url: '/sys/user/page', method: 'post', data }) }
export function addUser(data: Partial<SysUser>): Promise<void> { return request({ url: '/sys/user/add', method: 'post', data }) }
export function editUser(data: Partial<SysUser>): Promise<void> { return request({ url: '/sys/user/edit', method: 'post', data }) }
export function deleteUser(data: { id: number | string }): Promise<void> { return request({ url: '/sys/user/delete', method: 'post', data }) }
export function updateUserStatus(data: { userId: number | string; status: number }): Promise<void> { return request({ url: '/sys/user/updateStatus', method: 'post', data }) }
export function getUserRoles(data: { id: number | string }): Promise<(number | string)[]> { return request({ url: '/sys/user/roles', method: 'post', data }) }
export function assignUserRoles(data: { userId: number | string; roleIds: (number | string)[] }): Promise<void> { return request({ url: '/sys/user/assignRoles', method: 'post', data }) }
export function resetUserPwd(data: { userId: number | string; newPassword: string }): Promise<void> { return request({ url: '/sys/user/resetPwd', method: 'post', data }) }

// 角色管理
export function getRolePage(data: PageParams & Partial<SysRole>): Promise<TableResponse<SysRole>> { return request({ url: '/sys/role/page', method: 'post', data }) }
export function getRoleList(data?: Record<string, unknown>): Promise<SysRole[]> { return request({ url: '/sys/role/list', method: 'post', data }) }
export function addRole(data: Partial<SysRole>): Promise<void> { return request({ url: '/sys/role/add', method: 'post', data }) }
export function editRole(data: Partial<SysRole>): Promise<void> { return request({ url: '/sys/role/edit', method: 'post', data }) }
export function deleteRole(data: { id: number | string }): Promise<void> { return request({ url: '/sys/role/delete', method: 'post', data }) }
export function updateRoleStatus(data: { roleId: number | string; status: number }): Promise<void> { return request({ url: '/sys/role/updateStatus', method: 'post', data }) }

// 菜单管理
export function getMenuTree(data?: Record<string, unknown>): Promise<SysMenu[]> { return request({ url: '/sys/menu/tree', method: 'post', data }) }
export function addMenu(data: Partial<SysMenu>): Promise<void> { return request({ url: '/sys/menu/add', method: 'post', data }) }
export function editMenu(data: Partial<SysMenu>): Promise<void> { return request({ url: '/sys/menu/edit', method: 'post', data }) }
export function deleteMenu(data: { id: number | string }): Promise<void> { return request({ url: '/sys/menu/delete', method: 'post', data }) }
export function updateMenuStatus(data: { menuId: number | string; status: number }): Promise<void> { return request({ url: '/sys/menu/updateStatus', method: 'post', data }) }

// 字典管理
export function getDictPage(data: PageParams & Partial<SysDict>): Promise<TableResponse<SysDict>> { return request({ url: '/sys/dict/page', method: 'post', data }) }
export function getDictListByType(data: { dictType: string }): Promise<{ list: Record<string, string>[] }> { return request({ url: '/sys/dict/listByType', method: 'post', data }) }
export function addDict(data: Partial<SysDict>): Promise<void> { return request({ url: '/sys/dict/add', method: 'post', data }) }
export function editDict(data: Partial<SysDict>): Promise<void> { return request({ url: '/sys/dict/edit', method: 'post', data }) }
export function deleteDict(data: { id: number | string }): Promise<void> { return request({ url: '/sys/dict/delete', method: 'post', data }) }
export function updateDictStatus(data: { dictId: number | string; status: number }): Promise<void> { return request({ url: '/sys/dict/updateStatus', method: 'post', data }) }
export function batchImportDict(data: Partial<SysDict>[]): Promise<string> { return request({ url: '/sys/dict/batchImport', method: 'post', data }) }

// 操作日志
export function getOperLogPage(data: PageParams): Promise<TableResponse<Record<string, unknown>>> { return request({ url: '/sys/operLog/page', method: 'post', data }) }
export function clearOperLog(data?: Record<string, unknown>): Promise<void> { return request({ url: '/sys/operLog/clear', method: 'post', data }) }

// 登录日志
export function getLoginLogPage(data: PageParams & { userName?: string; status?: number }): Promise<TableResponse<Record<string, unknown>>> { return request({ url: '/sys/loginLog/page', method: 'post', data }) }
export function clearLoginLog(data?: Record<string, unknown>): Promise<void> { return request({ url: '/sys/loginLog/clear', method: 'post', data }) }

// 部门管理
export function getDeptTree(data?: Record<string, unknown>): Promise<SysDept[]> { return request({ url: '/sys/dept/tree', method: 'post', data }) }
export function addDept(data: Partial<SysDept>): Promise<void> { return request({ url: '/sys/dept/add', method: 'post', data }) }
export function editDept(data: Partial<SysDept>): Promise<void> { return request({ url: '/sys/dept/edit', method: 'post', data }) }
export function deleteDept(data: { id: number | string }): Promise<void> { return request({ url: '/sys/dept/delete', method: 'post', data }) }
export function updateDeptStatus(data: { deptId: number | string; status: number }): Promise<void> { return request({ url: '/sys/dept/updateStatus', method: 'post', data }) }

// 仪表盘统计
export function getDashboardStats(): Promise<{
  userCount: number
  roleCount: number
  menuCount: number
  dictCount: number
  deptCount: number
  postCount: number
  noticeCount: number
  onlineUserCount: number
  operLogTodayCount: number
}> { return request({ url: '/sys/dashboard/stats', method: 'post' }) }

export function getDashboardTrend(): Promise<{
  dates: string[]
  newUsers: number[]
  operations: number[]
}> { return request({ url: '/sys/dashboard/trend', method: 'post' }) }

export function getDashboardTypeDistribution(): Promise<Array<{ name: string; value: number }>> { return request({ url: '/sys/dashboard/typeDistribution', method: 'post' }) }

// 最近操作日志
export function getRecentOperLogs(params: PageParams & { module?: string }): Promise<TableResponse<{ module: string; operUserName: string; operType: string; operTime: string }>> { return request({ url: '/sys/operLog/recent', method: 'post', data: params }) }

// 岗位管理
export function getPostPage(data: PageParams & Partial<SysPost>): Promise<TableResponse<SysPost>> { return request({ url: '/sys/post/page', method: 'post', data }) }
export function getPostList(data?: Record<string, unknown>): Promise<SysPost[]> { return request({ url: '/sys/post/list', method: 'post', data }) }
export function addPost(data: Partial<SysPost>): Promise<void> { return request({ url: '/sys/post/add', method: 'post', data }) }
export function editPost(data: Partial<SysPost>): Promise<void> { return request({ url: '/sys/post/edit', method: 'post', data }) }
export function deletePost(data: { id: number | string }): Promise<void> { return request({ url: '/sys/post/delete', method: 'post', data }) }
export function updatePostStatus(data: { postId: number | string; status: number }): Promise<void> { return request({ url: '/sys/post/updateStatus', method: 'post', data }) }
