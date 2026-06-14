import request from '@/utils/request'
import type { PageParams, TableResponse } from '@/types/api'
import type { SysUser, SysRole, SysMenu, SysDept, SysDict } from '@/types/system'

// 用户管理
export function getUserPage(data: PageParams & Partial<SysUser>): Promise<TableResponse<SysUser>> { return request({ url: '/sys/user/page', method: 'post', data }) }
export function addUser(data: Partial<SysUser>): Promise<void> { return request({ url: '/sys/user/add', method: 'post', data }) }
export function editUser(data: Partial<SysUser>): Promise<void> { return request({ url: '/sys/user/edit', method: 'post', data }) }
export function deleteUser(data: { userId: number }): Promise<void> { return request({ url: '/sys/user/delete', method: 'post', data }) }
export function updateUserStatus(data: { userId: number; status: number }): Promise<void> { return request({ url: '/sys/user/updateStatus', method: 'post', data }) }
export function getUserRoles(data: { userId: number }): Promise<{ roles: SysRole[] }> { return request({ url: '/sys/user/roles', method: 'post', data }) }
export function assignUserRoles(data: { userId: number; roleIds: number[] }): Promise<void> { return request({ url: '/sys/user/assignRoles', method: 'post', data }) }

// 角色管理
export function getRolePage(data: PageParams & Partial<SysRole>): Promise<TableResponse<SysRole>> { return request({ url: '/sys/role/page', method: 'post', data }) }
export function getRoleList(data?: Record<string, unknown>): Promise<SysRole[]> { return request({ url: '/sys/role/list', method: 'post', data }) }
export function addRole(data: Partial<SysRole>): Promise<void> { return request({ url: '/sys/role/add', method: 'post', data }) }
export function editRole(data: Partial<SysRole>): Promise<void> { return request({ url: '/sys/role/edit', method: 'post', data }) }
export function deleteRole(data: { roleId: number }): Promise<void> { return request({ url: '/sys/role/delete', method: 'post', data }) }
export function updateRoleStatus(data: { roleId: number; status: number }): Promise<void> { return request({ url: '/sys/role/updateStatus', method: 'post', data }) }

// 菜单管理
export function getMenuTree(data?: Record<string, unknown>): Promise<SysMenu[]> { return request({ url: '/sys/menu/tree', method: 'post', data }) }
export function addMenu(data: Partial<SysMenu>): Promise<void> { return request({ url: '/sys/menu/add', method: 'post', data }) }
export function editMenu(data: Partial<SysMenu>): Promise<void> { return request({ url: '/sys/menu/edit', method: 'post', data }) }
export function deleteMenu(data: { menuId: number }): Promise<void> { return request({ url: '/sys/menu/delete', method: 'post', data }) }
export function updateMenuStatus(data: { menuId: number; status: number }): Promise<void> { return request({ url: '/sys/menu/updateStatus', method: 'post', data }) }

// 字典管理
export function getDictPage(data: PageParams & Partial<SysDict>): Promise<TableResponse<SysDict>> { return request({ url: '/sys/dict/page', method: 'post', data }) }
export function getDictListByType(data: { dictType: string }): Promise<{ list: Record<string, string>[] }> { return request({ url: '/sys/dict/listByType', method: 'post', data }) }
export function addDict(data: Partial<SysDict>): Promise<void> { return request({ url: '/sys/dict/add', method: 'post', data }) }
export function editDict(data: Partial<SysDict>): Promise<void> { return request({ url: '/sys/dict/edit', method: 'post', data }) }
export function deleteDict(data: { dictId: number }): Promise<void> { return request({ url: '/sys/dict/delete', method: 'post', data }) }
export function updateDictStatus(data: { dictId: number; status: number }): Promise<void> { return request({ url: '/sys/dict/updateStatus', method: 'post', data }) }

// 操作日志
export function getOperLogPage(data: PageParams): Promise<TableResponse<Record<string, unknown>>> { return request({ url: '/sys/operLog/page', method: 'post', data }) }
export function clearOperLog(data?: Record<string, unknown>): Promise<void> { return request({ url: '/sys/operLog/clear', method: 'post', data }) }

// 部门管理
export function getDeptTree(data?: Record<string, unknown>): Promise<SysDept[]> { return request({ url: '/sys/dept/tree', method: 'post', data }) }
export function addDept(data: Partial<SysDept>): Promise<void> { return request({ url: '/sys/dept/add', method: 'post', data }) }
export function editDept(data: Partial<SysDept>): Promise<void> { return request({ url: '/sys/dept/edit', method: 'post', data }) }
export function deleteDept(data: { deptId: number }): Promise<void> { return request({ url: '/sys/dept/delete', method: 'post', data }) }
export function updateDeptStatus(data: { deptId: number; status: number }): Promise<void> { return request({ url: '/sys/dept/updateStatus', method: 'post', data }) }
