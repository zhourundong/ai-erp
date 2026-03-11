import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 60000,  // AI 调用可能需要较长时间
})

api.interceptors.request.use((config) => {
  const authStorage = localStorage.getItem('auth-storage')
  if (authStorage) {
    const { state } = JSON.parse(authStorage)
    if (state?.token) {
      config.headers.Authorization = `Bearer ${state.token}`
    }
  }
  return config
})

export default api

export const authApi = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
  
  getUserInfo: () =>
    api.get('/auth/userinfo'),
  
  logout: () =>
    api.post('/auth/logout'),
}
