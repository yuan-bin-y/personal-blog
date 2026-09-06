import request, { responseData, setAccessToken } from './request'

export const getIdentity = () => request.get('/auth/me').then(responseData)

export const login = async (credentials) => {
  const data = await request.post('/auth/login', credentials).then(responseData)
  setAccessToken(data.accessToken)
  return data
}

export const logout = async () => {
  try {
    await request.post('/auth/logout')
  } finally {
    setAccessToken(null)
  }
}
