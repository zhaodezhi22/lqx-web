export const loadRememberedState = (key, defaults = {}) => {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return { ...defaults }
    const parsed = JSON.parse(raw)
    return { ...defaults, ...parsed }
  } catch (e) {
    return { ...defaults }
  }
}

export const saveRememberedState = (key, state) => {
  try {
    localStorage.setItem(key, JSON.stringify(state))
  } catch (e) {
    // Ignore storage errors to avoid breaking page interaction.
  }
}

export const clearRememberedState = (key) => {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    // Ignore storage errors.
  }
}
