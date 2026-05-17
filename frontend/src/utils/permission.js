export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try {
    return JSON.parse(userStr)
  } catch (e) {
    return null
  }
}

export const getCurrentRole = () => {
  return getCurrentUser()?.role ?? 0
}

export const hasRole = (role) => getCurrentRole() === role

export const hasAnyRole = (roles = []) => roles.includes(getCurrentRole())

export const isAdminRole = () => getCurrentRole() >= 2

export const isRootRole = () => getCurrentRole() === 3
