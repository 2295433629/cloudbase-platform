export interface UserInfo {
  userId: number
  account: string
  realName: string
  deptId: number
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
