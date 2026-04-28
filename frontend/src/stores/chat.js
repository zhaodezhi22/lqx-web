import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { clearChatSocket, setChatSocket } from '../utils/chatSocketManager'
import { getAuthExpiredEventName } from '../utils/auth'

export const useChatStore = defineStore('chat', () => {
  const socket = ref(null)
  const isConnected = ref(false)
  const currentChatUser = ref(null)
  const messages = ref({})
  const unreadCounts = ref({})
  const totalUnreadCount = computed(() => Object.values(unreadCounts.value).reduce((sum, count) => sum + count, 0))
  let reconnectTimer = null
  let allowReconnect = true

  const clearReconnectTimer = () => {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  const getMyUserId = () => {
    try {
      const userStr = localStorage.getItem('user')
      if (!userStr) return null
      const user = JSON.parse(userStr)
      return user.userId == null ? null : String(user.userId)
    } catch (e) {
      return null
    }
  }

  const addMessageToHistory = (userId, msg) => {
    const key = String(userId)
    if (!messages.value[key]) {
      messages.value[key] = []
    }
    messages.value[key].push(msg)
  }

  const getWsUrl = (token) => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    return `${protocol}//${host}/ws/chat?token=${encodeURIComponent(token)}`
  }

  const scheduleReconnect = () => {
    if (!allowReconnect || reconnectTimer) return
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      const latestToken = localStorage.getItem('token')
      if (!latestToken || !allowReconnect) return
      connect(latestToken)
    }, 1500)
  }

  const disconnect = ({ disableReconnect = false } = {}) => {
    if (disableReconnect) {
      allowReconnect = false
    }
    clearReconnectTimer()
    if (socket.value) {
      const currentSocket = socket.value
      socket.value = null
      clearChatSocket(currentSocket)
      if (currentSocket.readyState === WebSocket.OPEN || currentSocket.readyState === WebSocket.CONNECTING) {
        currentSocket.close(4001, 'CLIENT_DISCONNECT')
      }
    }
    isConnected.value = false
  }

  const connect = (token) => {
    if (!token) return
    allowReconnect = true
    if (socket.value && (socket.value.readyState === WebSocket.OPEN || socket.value.readyState === WebSocket.CONNECTING)) {
      return
    }

    const wsUrl = getWsUrl(token)
    socket.value = new WebSocket(wsUrl)
    setChatSocket(socket.value)

    socket.value.onopen = () => {
      isConnected.value = true
      console.log('WebSocket connected')
      clearReconnectTimer()
    }

    socket.value.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        handleReceiveMessage(msg)
      } catch (e) {
        console.error('Parse message failed:', e)
      }
    }

    socket.value.onclose = (event) => {
      isConnected.value = false
      console.log('WebSocket disconnected')
      clearChatSocket(socket.value)
      socket.value = null
      if (event?.code === 4001 || !localStorage.getItem('token')) {
        allowReconnect = false
        return
      }
      scheduleReconnect()
    }

    socket.value.onerror = (error) => {
      console.error('WebSocket error:', error)
    }
  }

  const sendMessage = (toId, content, type = 0) => {
    if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
      ElMessage.error('\u7f51\u7edc\u8fde\u63a5\u5df2\u65ad\u5f00')
      return false
    }

    const myId = getMyUserId()
    const clientMsgId = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    const msg = {
      toId,
      content,
      type,
      clientMsgId
    }

    socket.value.send(JSON.stringify(msg))

    const localMsg = {
      id: clientMsgId,
      clientMsgId,
      event: 'pending',
      fromId: myId,
      toId,
      content,
      type,
      createTime: new Date().toISOString(),
      isRecalled: 0,
      isSelf: true
    }

    addMessageToHistory(toId, localMsg)
    return true
  }

  const handleReceiveMessage = (msg) => {
    if (msg.event === 'recall' || msg.type === 2) {
      handleRecallMessage(msg)
      return
    }

    if (msg.event === 'ack') {
      const targetUserId = String(msg.toId)
      const list = messages.value[targetUserId] || []
      const local = list.find(item => item.clientMsgId && item.clientMsgId === msg.clientMsgId)
      if (local) {
        local.id = msg.id
        local.fromId = msg.fromId
        local.toId = msg.toId
        local.createTime = msg.createTime
        local.event = 'message'
        local.isRecalled = msg.isRecalled
      }
      return
    }

    const myId = getMyUserId()
    if (myId && String(msg.fromId) === myId) {
      return
    }

    const targetUserId = String(msg.fromId)
    const list = messages.value[targetUserId] || []
    const exists = list.some(item => String(item.id) === String(msg.id))
    if (exists) {
      return
    }

    const chatMsg = {
      ...msg,
      event: msg.event || 'message',
      isSelf: false
    }

    addMessageToHistory(targetUserId, chatMsg)

    ElNotification({
      title: '新消息',
      message: msg.type === 1 ? '[图片]' : (msg.content || '收到新消息'),
      duration: 2500
    })

    if (!currentChatUser.value || String(currentChatUser.value.userId) !== targetUserId) {
      unreadCounts.value[targetUserId] = (unreadCounts.value[targetUserId] || 0) + 1
    }
  }

  const handleRecallMessage = (msg) => {
    const messageId = msg.messageId == null ? null : String(msg.messageId)
    if (!messageId) {
      return
    }

    for (const uid in messages.value) {
      const list = messages.value[uid]
      const targetMsg = list.find(m => String(m.id) === messageId)
      if (targetMsg) {
        targetMsg.isRecalled = 1
        targetMsg.content = '\u8be5\u6d88\u606f\u5df2\u64a4\u56de'
        break
      }
    }
  }

  const setCurrentChatUser = (user) => {
    currentChatUser.value = user
    if (user) {
      unreadCounts.value[String(user.userId)] = 0
    }
  }

  const loadHistory = (userId, list) => {
    const myId = getMyUserId()
    const key = String(userId)
    messages.value[key] = list.map(m => ({
      ...m,
      event: 'message',
      isSelf: myId != null && String(m.fromId) === myId
    }))
  }

  if (typeof window !== 'undefined' && !window.__LQX_AUTH_EXPIRED_LISTENER__) {
    window.__LQX_AUTH_EXPIRED_LISTENER__ = true
    window.addEventListener(getAuthExpiredEventName(), () => {
      disconnect({ disableReconnect: true })
    })
  }

  return {
    socket,
    isConnected,
    currentChatUser,
    messages,
    unreadCounts,
    totalUnreadCount,
    connect,
    disconnect,
    sendMessage,
    setCurrentChatUser,
    loadHistory,
    handleRecallMessage
  }
})
