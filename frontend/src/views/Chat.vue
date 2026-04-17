<template>
  <div class="chat-container">
    <!-- 左侧好友列表 -->
    <div class="friend-list">
      <div class="list-header">
        <h3>消息</h3>
        <el-button type="primary" size="small" circle @click="showAddFriend = true">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      
      <div class="list-content">
        <!-- New Friends Item -->
        <div 
          class="friend-item"
          :class="{ active: showNewFriends }"
          @click="openNewFriends"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="40" :icon="UserFilled" style="background-color: #f0a020; color: white;" />
            <span v-if="newFriendCount > 0" class="badge">{{ newFriendCount }}</span>
          </div>
          <div class="info">
            <div class="name">新的朋友</div>
            <div class="last-msg">好友申请</div>
          </div>
        </div>

        <div 
          v-for="friend in friendList" 
          :key="friend.userId"
          class="friend-item"
          :class="{ active: currentChatUser?.userId === friend.userId }"
          @click="selectFriend(friend)"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="40" :src="friend.avatar || defaultAvatar" />
            <span v-if="unreadCounts[friend.userId]" class="badge">{{ unreadCounts[friend.userId] }}</span>
          </div>
          <div class="info">
            <div class="name">{{ friend.remark || friend.realName || friend.username }}</div>
            <div class="last-msg text-ellipsis">点击开始聊天</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-window" v-if="currentChatUser">
      <div class="window-header">
        <div class="user-info">
          <span class="name">{{ currentChatUser.remark || currentChatUser.realName || currentChatUser.username }}</span>
        </div>
        <div class="actions">
          <el-button link>
            <el-icon><More /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="message-area" ref="messageBox">
        <div class="message-list">
          <div 
            v-for="msg in currentMessages" 
            :key="msg.id" 
            class="message-item"
            :class="{ self: msg.isSelf }"
          >
            <div class="message-content-wrapper">
              <el-avatar class="avatar" :size="36" :src="msg.isSelf ? myAvatar : (currentChatUser.avatar || defaultAvatar)" />
              
              <div class="bubble-wrapper">
                <div 
                  class="bubble" 
                  :class="{ recalled: msg.isRecalled }"
                  @contextmenu.prevent="showContextMenu($event, msg)"
                >
                  <template v-if="msg.isRecalled">
                    <span class="recall-tip">{{ msg.content }}</span>
                  </template>
                  <template v-else>
                    <span v-if="msg.type === 0">{{ msg.content }}</span>
                    <el-image 
                      v-else-if="msg.type === 1" 
                      :src="msg.content" 
                      :preview-src-list="[msg.content]"
                      class="msg-image"
                    />
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="toolbar">
          <el-upload
            action="/api/file/upload"
            :show-file-list="false"
            :on-success="handleImageUpload"
            :before-upload="beforeImageUpload"
          >
            <el-button link><el-icon><Picture /></el-icon></el-button>
          </el-upload>
        </div>
        <el-input
          v-model="inputContent"
          type="textarea"
          :rows="3"
          placeholder="请输入消息..."
          @keydown.enter.prevent="handleSend"
          resize="none"
        />
        <div class="send-btn">
          <el-button type="primary" @click="handleSend" :disabled="!inputContent.trim()">发送</el-button>
        </div>
      </div>
    </div>

    <!-- New Friends Window -->
    <div class="new-friends-window" v-else-if="showNewFriends">
      <div class="window-header">
        <div class="user-info">
          <span class="name">好友申请</span>
        </div>
      </div>
      <div class="request-list">
        <div v-if="friendRequests.length === 0" class="empty-requests">
          <el-empty description="暂无新的好友申请" />
        </div>
        <div v-else v-for="req in friendRequests" :key="req.id" class="request-item">
          <div class="req-left">
            <el-avatar :size="50" :src="req.fromAvatar || defaultAvatar" />
            <div class="req-info">
              <div class="req-name">{{ req.fromName }}</div>
              <div class="req-reason">附言：{{ req.reason }}</div>
              <div class="req-time">{{ req.createTime }}</div>
            </div>
          </div>
          <div class="req-actions">
            <el-button type="success" size="small" @click="handleAccept(req)">
              <el-icon><Check /></el-icon> 同意
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(req)">
              <el-icon><Close /></el-icon> 拒绝
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <el-empty description="选择一个好友开始聊天" />
    </div>

    <!-- 右键菜单 -->
    <div 
      v-show="contextMenuVisible" 
      class="context-menu" 
      :style="{ top: contextMenuY + 'px', left: contextMenuX + 'px' }"
    >
      <div class="menu-item" @click="handleRecall">撤回</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { Plus, More, Picture, UserFilled, Check, Close } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat'
import { storeToRefs } from 'pinia'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const route = useRoute()
const chatStore = useChatStore()
const { currentChatUser, messages, unreadCounts } = storeToRefs(chatStore)

const friendList = ref([])
const friendRequests = ref([])
const showNewFriends = ref(false)
const newFriendCount = computed(() => friendRequests.value.length)
const inputContent = ref('')
const messageBox = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const myAvatar = ref(localStorage.getItem('avatar') || defaultAvatar) // 假设从本地获取头像

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const selectedMessage = ref(null)

const currentMessages = computed(() => {
  if (!currentChatUser.value) return []
  return messages.value[currentChatUser.value.userId] || []
})

// Auto scroll when messages change
watch(currentMessages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(async () => {
  // 连接 WebSocket
  const token = localStorage.getItem('token')
  if (token) {
    chatStore.connect(token)
  }

  // 加载好友列表
  await loadFriends()
  await loadFriendRequests()

  // Check for target user in query
  const targetId = route.query.userId
  if (targetId) {
    const friend = friendList.value.find(f => f.userId == targetId)
    if (friend) {
      selectFriend(friend)
    }
  }
  
  // Watch for unread counts change to re-sort or highlight (Optional)
  watch(unreadCounts, (newCounts) => {
    // We could re-sort friendList here if we want to move unread to top
  }, { deep: true })
  
  // 点击空白处关闭右键菜单
  document.addEventListener('click', closeContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeContextMenu)
})

const loadFriends = async () => {
  try {
    const res = await request.get('/im/friend/list')
    if (res.code === 200) {
      friendList.value = res.data
    }
  } catch (error) {
    console.error('Failed to load friends:', error)
  }
}

const loadFriendRequests = async () => {
  try {
    const res = await request.get('/im/friend/request/pending')
    if (res.code === 200) {
      friendRequests.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const openNewFriends = () => {
  showNewFriends.value = true
  chatStore.setCurrentChatUser(null)
}

const handleAccept = async (req) => {
  try {
    const res = await request.post('/im/friend/accept', { requestId: req.id })
    if (res.code === 200) {
      ElMessage.success('已同意好友申请')
      await loadFriendRequests()
      await loadFriends()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (req) => {
  try {
    const res = await request.post('/im/friend/reject', { requestId: req.id })
    if (res.code === 200) {
      ElMessage.success('已拒绝好友申请')
      await loadFriendRequests()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const selectFriend = async (friend) => {
  showNewFriends.value = false
  chatStore.setCurrentChatUser(friend)
  
  // 加载历史记录
  try {
    // 假设后端还没提供这个接口，先用 try catch
    // 这里需要后端配合增加 /api/im/chat/history 接口
    const res = await request.get(`/im/chat/history`, {
      params: { friendId: friend.userId, size: 20 }
    })
    if (res.code === 200) {
      chatStore.loadHistory(friend.userId, res.data)
    }
  } catch (e) {
    // 接口未就绪暂不处理
  }
  
  scrollToBottom()
}

const handleSend = () => {
  const content = inputContent.value.trim()
  if (!content) return

  if (chatStore.sendMessage(currentChatUser.value.userId, content, 0)) {
    inputContent.value = ''
    scrollToBottom()
  }
}

const handleImageUpload = (res) => {
  if (res.code === 200) {
    chatStore.sendMessage(currentChatUser.value.userId, res.data, 1) // type 1 图片
    scrollToBottom()
  }
}

const beforeImageUpload = (file) => {
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isLt2M
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageBox.value) {
      messageBox.value.scrollTop = messageBox.value.scrollHeight
    }
  })
}

// 右键菜单逻辑
const showContextMenu = (e, msg) => {
  // 只能撤回自己发送的消息，且未撤回
  if (!msg.isSelf || msg.isRecalled) return
  
  // 检查是否超过2分钟 (这里简单判断，实际后端也会校验)
  const createTime = new Date(msg.createTime).getTime()
  if (Date.now() - createTime > 120 * 1000) {
      return
  }

  selectedMessage.value = msg
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

const handleRecall = async () => {
  if (!selectedMessage.value) return
  
  try {
    const res = await request.post('/im/chat/recall', {
      messageId: selectedMessage.value.id
    })
    if (res.code === 200) {
      // 本地立即更新状态（也可以等待 socket 推送）
      selectedMessage.value.isRecalled = 1
      selectedMessage.value.content = '该消息已撤回'
      ElMessage.success('撤回成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
  }
  closeContextMenu()
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 100px); /* 减去顶部导航高度 */
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 20px;
}

/* 左侧列表 */
.friend-list {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.new-friends-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.request-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.request-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}
.request-item:hover {
  background: #fafafa;
}
.req-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.req-name {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}
.req-reason {
  color: #666;
  font-size: 14px;
  margin-top: 4px;
}
.req-time {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
.req-actions {
  display: flex;
  gap: 10px;
}

.list-header {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
  padding: 12px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background 0.3s;
}

.friend-item:hover, .friend-item.active {
  background: #f0f7ff;
}

.avatar-wrapper {
  position: relative;
  margin-right: 12px;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
  padding: 0 5px;
  font-size: 12px;
  transform: scale(0.8);
}

.info {
  flex: 1;
  overflow: hidden;
}

.name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.last-msg {
  font-size: 12px;
  color: #999;
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧聊天窗口 */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
}

.window-header {
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.window-header .name {
  font-size: 16px;
  font-weight: bold;
}

.message-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.message-item {
  margin-bottom: 20px;
  display: flex;
}

.message-item.self {
  justify-content: flex-end;
}

.message-content-wrapper {
  display: flex;
  max-width: 70%;
}

.message-item.self .message-content-wrapper {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  margin: 0 10px;
}

.bubble-wrapper {
  display: flex;
  flex-direction: column;
}

.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  position: relative;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.message-item.self .bubble {
  background: #95ec69; /* 微信绿 */
  background: #c23e3e; /* 柳琴红 */
  color: #fff;
}

.bubble.recalled {
  background: #e0e0e0 !important;
  color: #999 !important;
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 4px;
}

.msg-image {
  max-width: 200px;
  border-radius: 4px;
}

.input-area {
  background: #fff;
  border-top: 1px solid #e6e6e6;
  padding: 10px 20px;
}

.toolbar {
  margin-bottom: 8px;
}

.send-btn {
  text-align: right;
  margin-top: 10px;
}

.empty-state {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  border-radius: 4px;
  padding: 5px 0;
  z-index: 9999;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.menu-item:hover {
  background: #f5f5f5;
}
</style>
