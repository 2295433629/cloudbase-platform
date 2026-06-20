export interface UserInfo {
  userId: number | string
  account: string
  realName: string
  deptId: number | string
  avatar: string
}

export interface LoginParams {
  account: string
  password: string
  uuid: string
  captcha: string
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}
