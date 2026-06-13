import request from '@/utils/request'

export function loginApi(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function getCaptcha() {
  return request({ url: '/auth/captcha', method: 'get' })
}

export function logoutApi(data) {
  return request({ url: '/auth/logout', method: 'post', data })
}

// 个人中心
export function getProfile(data) {
  return request({ url: '/auth/profile', method: 'post', data })
}

export function updateProfile(data) {
  return request({ url: '/auth/updateProfile', method: 'post', data })
}

export function changePassword(data) {
  return request({ url: '/auth/changePassword', method: 'post', data })
}
