export interface SysUser {
  userId?: number | string
  account: string
  realName: string
  phone: string
  email: string
  deptId?: number | string
  postId?: number | string
  status: number
  avatar: string
  createTime?: string
}

export interface SysRole {
  roleId?: number | string
  roleName: string
  roleCode: string
  sort: number
  dataScope: number
  status: number
  remark: string
  createTime?: string
}

export interface SysMenu {
  menuId?: number | string
  parentId: number | string
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
  deptId?: number | string
  parentId: number | string
  deptName: string
  sort: number
  leader: string
  phone: string
  email: string
  status: number
  children?: SysDept[]
}

export interface SysDict {
  dictId?: number | string
  dictType: string
  dictLabel: string
  dictValue: string
  sort: number
  status: number
  remark: string
}

export interface SysPost {
  postId?: number | string
  postCode: string
  postName: string
  sort: number
  status: number
  remark: string
  createTime?: string
}
