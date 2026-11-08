import request from '../utils/axios'

// 上传图片，返回 { url: '/uploads/xxx.jpg' }
export const uploadImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload/image', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
