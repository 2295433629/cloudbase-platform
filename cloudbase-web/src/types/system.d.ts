export interface SysUser {
  userId?: number
  account: string
  realName: string
  phone: string
  email: string
  deptId?: number
  status: number
  avatar: string
  createTime?: string
}

export interface SysRole {
  roleId?: number
  roleName: string
  roleKey: string
  roleSort: number
  dataScope: number
  status: number
  remark: string
  createTime?: string
}

export interface SysMenu {
  menuId?: number
  parentId: number
  menuName: string
  menuType: number
  path: string
  component: string
  perms: string
  icon: string
  sort: number
  hidden?: number
  status: number
  children?: SysMenu[]
}

export interface SysDept {
  deptId?: number
  parentId: number
  deptName: string
  sort: number
  leader: string
  phone: string
  email: string
  status: number
  children?: SysDept[]
}

export interface SysDict {
  dictId?: number
  dictName: string
  dictType: string
  status: number
  remark: string
}
