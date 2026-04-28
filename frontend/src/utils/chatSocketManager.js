let currentSocket = null

export const setChatSocket = (socket) => {
  currentSocket = socket
}

export const clearChatSocket = (socket) => {
  if (!socket || currentSocket === socket) {
    currentSocket = null
  }
}

export const closeChatSocket = (code = 4001, reason = 'TOKEN_EXPIRED') => {
  if (!currentSocket) return
  const socket = currentSocket
  currentSocket = null
  if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
    socket.close(code, reason)
  }
}
