import { computed, reactive } from 'vue'
import * as authApi from '../api/auth'
import { apiMessage, getAccessToken } from '../api/request'

/** 全站唯一身份状态；不再通过 query 或组件硬编码 Owner。 */
const ownerState = reactive({
  ready: false,
  loading: false,
  isOwner: false,
  identity: null,
  permissions: [],
  error: '',
})

let initialization

const applyIdentity = (identity) => {
  ownerState.identity = identity
  ownerState.isOwner = identity?.authenticated === true && identity?.role === 'OWNER'
  ownerState.permissions = identity?.permissions || []
}

const initializeOwnerMode = async () => {
  if (initialization) return initialization
  initialization = (async () => {
    try {
      applyIdentity(await authApi.getIdentity())
    } catch {
      applyIdentity(null)
    } finally {
      ownerState.ready = true
    }
  })()
  return initialization
}

const loginOwner = async (credentials) => {
  ownerState.loading = true
  ownerState.error = ''
  try {
    const result = await authApi.login(credentials)
    applyIdentity(result.identity)
    return result.identity
  } catch (error) {
    ownerState.error = apiMessage(error)
    throw error
  } finally {
    ownerState.loading = false
  }
}

const logoutOwner = async () => {
  try {
    if (getAccessToken()) await authApi.logout()
  } finally {
    applyIdentity(null)
  }
}

export const useOwnerMode = () => ({
  state: ownerState,
  isOwner: computed(() => ownerState.isOwner),
  ready: computed(() => ownerState.ready),
  initializeOwnerMode,
  loginOwner,
  logoutOwner,
  hasPermission: (permission) => ownerState.permissions.includes(permission),
})
