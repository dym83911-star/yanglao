import request from '../utils/axios'
import { setToken, setUser, clearAuth } from '../utils/session'

// 登录
export const login = async (phone, password) => {
  const res = await request.post('/auth/login', { phone, password })
  if (res.code === 200) {
    setToken(res.data.token)
    setUser(res.data.user)
  }
  return res
}

// 注册（后端仅返回成功标记，不含 token，注册后需手动登录）
export const register = async (name, phone, password) => {
  return request.post('/auth/register', { name, phone, password })
}

// 忘记密码：手机号 + 新密码 直接重置
export const resetPassword = async (phone, newPassword) => {
  return request.put('/auth/reset-password', { phone, newPassword })
}

// 发送登录验证码（演示环境后端会在 data 中返回验证码）
export const sendSmsCode = async (phone) => {
  return request.post('/auth/sms-code', { phone })
}

// 验证码登录
export const loginBySms = async (phone, code) => {
  const res = await request.post('/auth/login-sms', { phone, code })
  if (res.code === 200) {
    setToken(res.data.token)
    setUser(res.data.user)
  }
  return res
}

// 当前用户
export const getCurrentUser = async () => {
  const res = await request.get('/auth/me')
  if (res.code === 200) {
    setUser(res.data)
  }
  return res
}

// 登出
export const logout = async () => {
  try {
    await request.post('/auth/logout')
  } catch (e) { /* 忽略后端可能的未登录错误 */ }
  clearAuth()
  return { code: 200, message: '退出成功' }
}

// 更新用户
export const updateUser = async (userData) => {
  const res = await request.put('/user', userData)
  if (res.code === 200) {
    setUser(res.data)
  }
  return res
}

// 修改密码
export const changePassword = async (oldPassword, newPassword) => {
  return request.put('/user/password', { oldPassword, newPassword })
}
