import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useChatStore = defineStore('chat', () => {
  const socket = ref(null)
  const isConnected = ref(false)
  const currentChatUser = ref(null) // 当前正在聊天的用户
  const messages = ref({}) // 消息记录，key为userId，value为消息数组
  const unreadCounts = ref({}) // 未读消息数，key为userId
  const totalUnreadCount = computed(() => Object.values(unreadCounts.value).reduce((sum, count) => sum + count, 0))

  // 初始化 WebSocket 连接
  const connect = (token) => {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) return

    // 使用 ws 协议连接，端口根据后端配置
    const wsUrl = `ws://localhost:8081/ws/chat?token=${token}`
    socket.value = new WebSocket(wsUrl)

    socket.value.onopen = () => {
      isConnected.value = true
      console.log('WebSocket connected')
    }

    socket.value.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        handleReceiveMessage(msg)
      } catch (e) {
        console.error('Parse message failed:', e)
      }
    }

    socket.value.onclose = () => {
      isConnected.value = false
      console.log('WebSocket disconnected')
      // 可以在这里实现重连逻辑
    }

    socket.value.onerror = (error) => {
      console.error('WebSocket error:', error)
    }
  }

  // 发送消息
  const sendMessage = (toId, content, type = 0) => {
    if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
      ElMessage.error('网络连接已断开')
      return false
    }

    const msg = {
      toId,
      content,
      type
    }
    
    socket.value.send(JSON.stringify(msg))
    
    // 立即添加到本地消息列表（乐观更新）
    const localMsg = {
      id: Date.now(), // 临时ID
      fromId: 'me', // 标识是自己发的
      toId,
      content,
      type,
      createTime: new Date().toISOString(),
      isRecalled: 0,
      isSelf: true
    }
    
    // 注意：如果是发给 toId 的消息，应该添加到 toId 的聊天记录中
    // 之前的 bug 可能是没注意这个逻辑，导致消息显示错位
    addMessageToHistory(toId, localMsg)
    return true
  }

  // 处理接收到的消息
  const handleReceiveMessage = (msg) => {
    // 撤回消息处理
    if (msg.type === 2) {
        handleRecallMessage(msg)
        return
    }

    // 处理消息确认 (ACK)，更新本地临时消息的真实ID
    if (msg.id) {
        // 尝试在所有会话中找到 createTime 匹配且是自己发送的消息
        // 这里简化：假设 msg 是完整的 ImChatMessage
        // 如果 msg.fromId 是我，说明是我发的消息的回执
        // 但前端不知道 "我" 的 ID，除非存在 store 里
        // 我们通过 currentChatUser 或 localStorage 获取当前用户 ID
        // 暂时简单遍历
        
        // 实际上，后端推送的消息格式如果是 { ...msg, fromId: me }
        // 我们可以根据内容和时间来匹配
        
        // 这里我们主要处理“接收别人发来的消息”
        // 对于“自己发的消息回执”，我们需要更新本地 ID
        // 由于前端目前没有存储 myUserId，我们先略过严格校验
        
        // 查找是否有匹配的临时消息（根据内容和时间）
        for (const uid in messages.value) {
            const list = messages.value[uid]
            // 倒序查找，找最近一条
            for (let i = list.length - 1; i >= 0; i--) {
                const local = list[i]
                if (local.isSelf && local.content === msg.content && local.type === msg.type) {
                    // 找到了，更新 ID
                    // local.id 是临时ID (timestamp, Number)
                    // msg.id 是真实ID (可能是 String 或 Number)
                    // 只要不相等，且 local.id 是临时 ID (时间戳通常很大)，就更新
                    if (local.id != msg.id) {
                         local.id = msg.id
                         return // 更新完就退出
                    }
                }
            }
        }
    }

    // 区分消息是“发送给我的”还是“我发送的”
    // 如果是自己发的（通过 WebSocket 回执收到），且上面没匹配到（或者已经匹配更新了），就不再添加了
    // 只有当消息是别人发给我的，才添加
    // 前端判断：msg.fromId != myId? 
    // 但我们没有 myId。
    // 我们可以依赖 isSelf 字段（如果后端返回了）或者 msg.fromId
    
    // 假设后端推送的 msg.fromId 是发送者 ID
    // 我们需要知道当前登录用户的 ID。
    let myId = null
    try {
        const userStr = localStorage.getItem('user')
        if (userStr) {
            myId = JSON.parse(userStr).userId
        }
    } catch(e) {}

    if (myId && String(msg.fromId) === String(myId)) {
        // 是自己发的消息回执，已经在上面处理了 ID 更新，直接返回
        return
    }

    // 别人发给我的
    const targetUserId = msg.fromId
    const chatMsg = {
        ...msg,
        isSelf: false
    }

    addMessageToHistory(targetUserId, chatMsg)

    // 如果不是当前聊天窗口，增加未读数
    if (!currentChatUser.value || currentChatUser.value.userId !== targetUserId) {
        unreadCounts.value[targetUserId] = (unreadCounts.value[targetUserId] || 0) + 1
        // Play sound or show browser notification
        ElMessage.info(`收到新消息`)
    }
  }

  // 处理撤回消息
  const handleRecallMessage = (msg) => {
      // msg 包含 messageId, fromId
      // 找到对应的消息并更新状态
      const userId = msg.fromId // 消息发送者ID，即当前聊天对象ID（如果是我发的，后端会推给我自己吗？逻辑上需要区分）
      
      // 这里简化处理：遍历所有消息列表查找并更新
      // 实际场景中，后端推送的 fromId 是撤回操作的发起者
      // 如果是我撤回的，后端应该也会推送给我，fromId 就是我自己
      
      // 我们需要遍历 messages 中的所有会话来找到这条消息
      for (const uid in messages.value) {
          const list = messages.value[uid]
          const targetMsg = list.find(m => m.id == msg.messageId)
          if (targetMsg) {
              targetMsg.isRecalled = 1
              targetMsg.content = '该消息已撤回'
              break
          }
      }
  }

  // 添加消息到历史记录
  const addMessageToHistory = (userId, msg) => {
    if (!messages.value[userId]) {
      messages.value[userId] = []
    }
    messages.value[userId].push(msg)
  }
  
  // 设置当前聊天对象
  const setCurrentChatUser = (user) => {
      currentChatUser.value = user
      if (user) {
          unreadCounts.value[user.userId] = 0
      }
  }

  // 加载历史记录 (需调用后端API)
  const loadHistory = (userId, list) => {
      messages.value[userId] = list.map(m => ({
          ...m,
          isSelf: m.fromId !== userId // 如果消息来自对方，则不是自己发的；否则是自己发的
      }))
  }

  return {
    socket,
    isConnected,
    currentChatUser,
    messages,
    unreadCounts,
    totalUnreadCount,
    connect,
    sendMessage,
    setCurrentChatUser,
    loadHistory,
    handleRecallMessage
  }
})
