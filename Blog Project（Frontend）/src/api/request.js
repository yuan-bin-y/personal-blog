import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const TOKEN_KEY = 'binspace_owner_token'

export const getAccessToken = () => sessionStorage.getItem(TOKEN_KEY)
export const setAccessToken = (token) => token
  ? sessionStorage.setItem(TOKEN_KEY, token)
  : sessionStorage.removeItem(TOKEN_KEY)

request.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) setAccessToken(null)
    return Promise.reject(error)
  },
)

export const responseData = (response) => response.data.data
export const apiMessage = (error) => error.response?.data?.message || error.message || '请求失败'

export default request
