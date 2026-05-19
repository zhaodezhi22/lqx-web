<template>
  <div class="page">
    <el-row :gutter="20">
      <el-col :span="6" class="profile-sidebar">
        <el-card class="user-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
            <div class="role-badge">
              <el-tag v-if="user.role === 1" type="warning" effect="dark">非遗传承人</el-tag>
              <el-tag v-else-if="user.role === 4" type="success" effect="dark">学徒</el-tag>
              <el-tag v-else-if="user.role === 2" type="danger">管理员</el-tag>
              <el-tag v-else-if="user.role === 3" type="danger" effect="dark">超级管理员</el-tag>
              <el-tag v-else type="info">普通用户</el-tag>
            </div>
          </div>
          <h3 class="username">{{ user.username }}</h3>
          <div class="info-list">
            <p><el-icon><User /></el-icon> {{ user.realName || '未实名' }}</p>
            <p><el-icon><Phone /></el-icon> {{ user.phone || '无电话' }}</p>
          </div>
          <div class="actions">
             <el-button v-if="user.role === 0" type="primary" plain block @click="$router.push('/inheritor/apply')">
              申请成为传承人
            </el-button>
            <el-button v-if="user.role === 1" type="success" plain block @click="$router.push('/inheritor/center')">
              进入传承人工作台
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="content-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="profile">
              <el-form label-width="100px" style="max-width: 500px">
                <el-form-item label="头像">
                  <el-upload
                    class="avatar-uploader"
                    action="/api/file/upload"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                  >
                    <img v-if="user.avatar" :src="user.avatar" class="avatar-edit" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                    <template #tip>
                      <div class="el-upload__tip">
                        点击图片上传，建议尺寸 200x200
                      </div>
                    </template>
                  </el-upload>
                </el-form-item>
                <el-form-item label="账号">
                  <el-input v-model="user.username" disabled />
                </el-form-item>
                <el-form-item label="真实姓名">
                  <el-input v-model="user.realName" />
                </el-form-item>
                <el-form-item label="联系电话">
                  <el-input v-model="user.phone" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="user.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile">保存修改</el-button>
                  <el-button type="warning" plain @click="changePwdDialogVisible = true">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="我的订单" name="orders">
              <el-table :data="orders" v-loading="loading" style="width: 100%" class="custom-table">
                <el-table-column label="订单信息" width="220">
                  <template #default="{ row }">
                    <div class="order-info-cell">
                      <div class="order-no">{{ row.orderNo }}</div>
                      <div class="create-time">{{ row.createTime }}</div>
                      <div v-if="row.addressSnapshot" style="font-size: 12px; color: #666; margin-top: 5px; display: flex; align-items: center;">
                         <el-icon style="margin-right: 4px;"><Location /></el-icon>
                         <span :title="row.addressSnapshot" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; display: inline-block; vertical-align: middle;">{{ row.addressSnapshot }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="商品" min-width="280">
                  <template #default="{ row }">
                    <div v-for="item in row.items" :key="item.id" class="product-item-flex" @click="openDetail(item.productId)">
                      <el-image :src="item.productImage" class="product-thumb" fit="cover">
                         <template #error>
                           <div class="image-slot"><el-icon><Picture /></el-icon></div>
                         </template>
                      </el-image>
                      <div class="product-meta">
                        <div class="product-name">{{ item.productName }}</div>
                        <div class="product-qty">x {{ item.quantity }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column prop="totalAmount" label="实付" width="100">
                   <template #default="{ row }">
                     <span class="price-text">¥{{ row.totalAmount }}</span>
                   </template>
                </el-table-column>

                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="statusTagType(row.status)" size="small" effect="light" round>{{ statusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 5px;">
                        <el-button v-if="row.status === 1" type="danger" link size="small" @click="handleMallRefund(row)">申请退款</el-button>
                        <el-button v-if="row.status === 2" type="primary" size="small" @click="handleConfirmReceipt(row)">确认收货</el-button>
                        <el-button v-if="row.status >= 2" type="primary" link size="small" @click="handleViewLogistics(row)">查看物流</el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="!loading && (!orders || orders.length === 0)" style="margin-top: 16px">
                <el-empty description="暂无订单" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="我的票务" name="tickets">
              <el-table :data="tickets" v-loading="ticketsLoading" style="width: 100%">
                <el-table-column prop="eventTitle" label="演出名称" min-width="150" />
                <el-table-column prop="showTime" label="演出时间" width="170">
                   <template #default="{ row }">
                     {{ formatTime(row.showTime) }}
                   </template>
                </el-table-column>
                <el-table-column prop="seatInfo" label="座位" width="100" />
                <el-table-column prop="price" label="票价" width="80" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag type="success" v-if="row.status === 1">已支付</el-tag>
                    <el-tag type="success" v-else-if="row.status === 2">已完成</el-tag>
                    <el-tag type="warning" v-else-if="row.status === 0">待支付</el-tag>
                    <el-tag type="warning" v-else-if="row.status === 3">退款审核中</el-tag>
                    <el-tag type="info" v-else-if="row.status === 4">已退票</el-tag>
                    <el-tag type="danger" v-else>已取消</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="剩余支付时间" width="130">
                  <template #default="{ row }">
                    <span v-if="row.status === 0" class="ticket-countdown">{{ formatTicketCountdown(row) }}</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="$router.push(`/events/${row.eventId}`)">查看演出</el-button>
                    <el-button link type="success" v-if="row.status === 1" @click="openVoucher(row)">查看凭证</el-button>
                    <el-button link type="danger" v-if="row.status === 1" @click="handleRefund(row)">退票</el-button>
                    <el-button link type="primary" v-if="row.status === 0" @click="goToTicketPay(row)">去支付</el-button>
                  </template>
                </el-table-column>
              </el-table>
               <div v-if="!ticketsLoading && (!tickets || tickets.length === 0)" style="margin-top: 16px">
                <el-empty description="暂无票务" />
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的积分" name="points">
              <div class="points-header">
                <div class="current-points">
                  <div class="label">
                    当前积分
                    <el-tooltip placement="top">
                      <template #content>
                        <div>积分获取规则：</div>
                        <div>1. 每日签到：+5积分 (连续签到有额外奖励)</div>
                        <div>2. 社区活跃：+10积分 (发帖/回帖，每日限3次)</div>
                        <div>3. 戏曲资源：+10积分 (观看>15s，每日限3次)</div>
                        <div>4. 购物返利：1元 = 1积分</div>
                        <div>5. 活动参与：+100积分 (购票核销)</div>
                      </template>
                      <el-icon class="info-icon"><Warning /></el-icon>
                    </el-tooltip>
                  </div>
                  <div class="value">{{ pointsInfo.currentPoints || 0 }}</div>
                </div>
                <div class="sign-in-section">
                  <el-button 
                    type="primary" 
                    size="large" 
                    :disabled="pointsInfo.signedIn" 
                    @click="handleSignIn"
                  >
                    {{ pointsInfo.signedIn ? '已签到' : '立即签到' }}
                  </el-button>
                  <div class="streak-info">
                    已连续签到 <span class="highlight">{{ pointsInfo.continuousSignDays || 0 }}</span> 天
                    <el-progress 
                      :percentage="calculateStreakPercentage(pointsInfo.continuousSignDays)" 
                      :format="streakFormat"
                      :status="pointsInfo.signedIn ? 'success' : ''"
                      style="margin-top: 8px; width: 200px"
                    />
                    <div class="streak-tips">
                      10天/20天/满月均有额外大奖！
                    </div>
                  </div>
                </div>
              </div>

              <el-divider content-position="left">积分明细</el-divider>

              <el-table :data="pointsLogs" v-loading="pointsLoading" stripe style="width: 100%">
                <el-table-column prop="createTime" label="时间" width="180">
                  <template #default="{ row }">
                    {{ formatTime(row.createdTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="changePoint" label="变动" width="120">
                  <template #default="{ row }">
                    <span :class="row.changePoint > 0 ? 'plus-point' : 'minus-point'">
                      {{ row.changePoint > 0 ? '+' : '' }}{{ row.changePoint }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="reason" label="原因" />
              </el-table>
              <div style="margin-top: 16px; text-align: right">
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="pointsTotal"
                  :page-size="10"
                  @current-change="handlePointsPageChange"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="我的地址" name="address">
              <div style="margin-bottom: 15px;">
                <el-button type="primary" :icon="Plus" @click="handleAddAddress">新增地址</el-button>
              </div>
              <el-table :data="addressList" v-loading="addressLoading" style="width: 100%">
                <el-table-column prop="receiverName" label="收货人" width="120" />
                <el-table-column prop="phone" label="手机号" width="150" />
                <el-table-column label="所在地区" min-width="150">
                    <template #default="{ row }">
                        {{ row.province }} {{ row.city }} {{ row.district }}
                    </template>
                </el-table-column>
                <el-table-column prop="detailAddress" label="详细地址" min-width="200" />
                <el-table-column label="默认" width="80">
                    <template #default="{ row }">
                        <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" :icon="Edit" @click="handleEditAddress(row)">编辑</el-button>
                        <el-button link type="danger" :icon="Delete" @click="handleDeleteAddress(row.id)">删除</el-button>
                        <el-button v-if="!row.isDefault" link type="warning" @click="handleSetDefaultAddress(row.id)">设为默认</el-button>
                    </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane v-if="showApprenticeshipTab" label="我的师门" name="apprenticeship">
              <div class="apprenticeship-container">
                <div class="apprenticeship-header">
                  <el-alert
                    v-if="hasMaster && currentMaster"
                    :title="`当前师父：${currentMaster.name || '暂无'}`"
                    type="info"
                    show-icon
                    :closable="false"
                  />
                  <el-alert
                    v-else
                    title="当前暂无有效师父关系，仍可继续查看历史师徒任务。"
                    type="warning"
                    show-icon
                    :closable="false"
                  />
                  <el-button
                    v-if="hasMaster"
                    type="danger"
                    plain
                    :disabled="!!pendingUnbindApply"
                    @click="applyUnbindMaster"
                  >
                    {{ pendingUnbindApply ? '解除申请审核中' : '申请解除师父' }}
                  </el-button>
                </div>
                <el-alert
                  v-if="pendingUnbindApply"
                  class="pending-unbind-alert"
                  type="warning"
                  show-icon
                  :closable="false"
                  :title="`解除申请审核中：${pendingUnbindApply.masterName || '当前师父'}`"
                  :description="pendingUnbindApply.reason || '已提交解除申请，等待师父处理。'"
                />
                <el-table :data="assignments" v-loading="assignmentsLoading" style="width: 100%">
                  <el-table-column prop="taskTitle" label="作业题目" min-width="150" />
                  <el-table-column prop="masterName" label="授课师父" width="120" />
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.status === 0" type="info">待提交</el-tag>
                      <el-tag v-else-if="row.status === 1" type="primary">已提交</el-tag>
                      <el-tag v-else-if="row.status === 2" type="success">已点评</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="openSubmit(row)">
                        {{ row.status === 0 ? '去提交' : (row.status === 1 ? '查看/修改' : '查看详情') }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="!assignmentsLoading && (!assignments || assignments.length === 0)" style="margin-top: 16px">
                   <el-empty description="暂无作业任务" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- Voucher Dialog -->
    <el-dialog v-model="voucherDialogVisible" title="电子票凭证" width="360px">
      <div style="text-align: center; padding: 20px;">
        <div style="font-size: 16px; color: #666; margin-bottom: 10px;">请向工作人员出示此码</div>
        <div style="font-size: 32px; font-weight: bold; color: #409EFF; letter-spacing: 2px; margin: 20px 0; border: 2px dashed #409EFF; padding: 10px; border-radius: 8px;">
          {{ currentVoucher }}
        </div>
        <div style="margin-top: 20px;">
           <img :src="`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${currentVoucher}`" style="width: 200px; height: 200px;" />
        </div>
        <div style="font-size: 12px; color: #999; margin-top: 10px;">核销码仅限使用一次</div>
      </div>
    </el-dialog>

    <!-- Homework Submit Dialog -->
    <el-dialog v-model="submitDialogVisible" :title="submitForm.status === 2 ? '作业详情 (已点评)' : '提交作业'" width="600px">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="演示素材" v-if="submitForm.demoVideoUrl">
           <div v-if="isImage(submitForm.demoVideoUrl)">
              <el-image 
                :src="submitForm.demoVideoUrl" 
                :preview-src-list="[submitForm.demoVideoUrl]"
                fit="contain"
                style="max-height: 300px; width: 100%" 
              />
           </div>
           <div v-else>
              <video :src="submitForm.demoVideoUrl" controls style="width: 100%; max-height: 300px; background: #000"></video>
           </div>
           <div class="tip" style="font-size: 12px; color: #999">师父的演示素材</div>
        </el-form-item>
        <el-form-item label="作业说明">
           <div style="background: #f5f7fa; padding: 10px; border-radius: 4px; width: 100%">{{ submitForm.taskDescription }}</div>
        </el-form-item>
        
        <el-divider />

        <el-form-item label="作业心得">
          <el-input 
            v-model="submitForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请填写作业心得或说明..." 
          />
        </el-form-item>
        <el-form-item label="作业文件">
          <el-upload
            v-if="submitForm.status !== 2"
            class="upload-demo"
            action="/api/file/upload"
            :limit="1"
            :on-success="handleHomeworkUploadSuccess"
            :before-upload="beforeHomeworkUpload"
            :show-file-list="true"
            :disabled="submitForm.status === 2"
          >
             <el-button type="primary" :disabled="submitForm.status === 2">
               {{ submitForm.status === 2 ? '作业已点评，不可修改' : '点击上传文件' }}
             </el-button>
             <template #tip>
               <div class="el-upload__tip">
                 {{ submitForm.videoUrl ? '已上传文件' : '支持图片/视频/音频，最大500MB' }}
               </div>
             </template>
          </el-upload>
          
          <div v-if="submitForm.videoUrl" style="margin-top: 10px;">
             <div v-if="isImage(submitForm.videoUrl)">
                <el-image 
                  :src="submitForm.videoUrl" 
                  :preview-src-list="[submitForm.videoUrl]"
                  fit="contain"
                  style="max-height: 200px; max-width: 100%; border: 1px solid #eee; border-radius: 4px;" 
                />
             </div>
             <div v-else>
                <video :src="submitForm.videoUrl" controls style="max-width: 100%; max-height: 200px; background: #000"></video>
             </div>
          </div>

          <div v-if="submitForm.status === 2" style="margin-top: 10px;">
             <el-tag type="info" effect="plain" style="width: 100%; text-align: center;">
                作业已点评，不可修改
             </el-tag>
          </div>
        </el-form-item>
        
        <div v-if="submitForm.status === 2">
            <el-divider>师父点评</el-divider>
            <el-form-item label="综合评分">
               <el-rate
                    v-model="submitForm.score"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value} 分"
                />
            </el-form-item>
            <el-form-item label="师父寄语">
                <div style="background: #fdf6ec; padding: 15px; border-radius: 8px; border: 1px solid #faecd8; width: 100%">
                    <div style="font-weight: bold; color: #e6a23c; margin-bottom: 5px;">
                        <el-icon><ChatLineSquare /></el-icon> 恩师留言
                    </div>
                    <div style="line-height: 1.6; color: #606266;">
                        {{ submitForm.reviewContent || '暂无评语' }}
                    </div>
                </div>
            </el-form-item>
            <div style="margin-top: 10px; font-size: 12px; color: #999; text-align: right;">
                点评时间：{{ formatTime(submitForm.reviewTime) }}
            </div>
        </div>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="doSubmit" :disabled="submitForm.status === 2" v-if="submitForm.status !== 2">提交作业</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Review Detail Dialog (Removed) -->
    <!-- Address Edit Dialog -->
    <el-dialog v-model="addressDialogVisible" :title="addressForm.id ? '编辑地址' : '新增地址'" width="500px">
        <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="100px">
            <el-form-item label="收货人" prop="receiverName">
                <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
                <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="省市区" prop="selectedArea">
                <el-cascader
                    v-model="addressForm.selectedArea"
                    :options="regionData"
                    placeholder="请选择省市区"
                    @change="handleAreaChange"
                    style="width: 100%"
                />
            </el-form-item>
            <el-form-item label="详细地址" prop="detailAddress">
                <el-input v-model="addressForm.detailAddress" type="textarea" placeholder="请输入详细地址" />
            </el-form-item>
            <el-form-item label="默认地址">
                <el-switch v-model="addressForm.isDefault" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="addressDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAddress">确定</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- Change Password Dialog -->
    <el-dialog v-model="changePwdDialogVisible" title="修改密码" width="400px">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="changePwdDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitChangePassword">确认修改</el-button>
            </span>
        </template>
    </el-dialog>

    <ProductDetailModal v-model:visible="detailVisible" :product-id="currentProductId" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { User, Phone, ArrowDown, Picture, Camera, Plus, Warning, Edit, Delete, Location, ChatLineSquare } from '@element-plus/icons-vue'
import ProductDetailModal from '../components/ProductDetailModal.vue'

import { regionData, codeToText } from 'element-china-area-data'

const route = useRoute()
const router = useRouter()
const activeTab = ref('profile')
const user = reactive({
  userId: null,
  username: '',
  role: 0,
  realName: '',
  phone: '',
  avatar: ''
})

const changePwdDialogVisible = ref(false)
const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})
const passwordRules = {
    oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        { 
            validator: (rule, value, callback) => {
                if (value !== passwordForm.newPassword) {
                    callback(new Error('两次输入密码不一致'))
                } else {
                    callback()
                }
            }, 
            trigger: 'blur' 
        }
    ]
}
const passwordFormRef = ref(null)

const submitChangePassword = async () => {
    if (!passwordFormRef.value) return
    await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
            try {
                const res = await request.put(`/users/${user.userId}/password/change`, {
                    oldPassword: passwordForm.oldPassword,
                    newPassword: passwordForm.newPassword
                })
                if (res.code === 200) {
                    ElMessage.success('密码修改成功，请重新登录')
                    changePwdDialogVisible.value = false
                    // Logout
                    localStorage.removeItem('token')
                    localStorage.removeItem('user')
                    router.push('/login')
                } else {
                    ElMessage.error(res.message || '修改失败')
                }
            } catch (e) {
                ElMessage.error(e.response?.data?.message || '修改失败')
            }
        }
    })
}

const detailVisible = ref(false)
const currentProductId = ref(null)

const orders = ref([])
const loading = ref(false)

const openDetail = (productId) => {
  currentProductId.value = productId
  detailVisible.value = true
}

const statusText = (s) => {
  switch (s) {
    case 0: return '待支付'
    case 1: return '待发货'
    case 2: return '已发货'
    case 3: return '已取消'
    case 4: return '退款审核中'
    case 5: return '已退款'
    case 6: return '已完成'
    default: return '未知'
  }
}
const statusTagType = (s) => {
  switch (s) {
    case 0: return 'warning'
    case 1: return 'info'
    case 2: return 'primary'
    case 3: return 'danger'
    case 4: return 'warning'
    case 5: return 'info'
    case 6: return 'success'
    default: return ''
  }
}

const tickets = ref([])
const ticketsLoading = ref(false)
const ticketNowTs = ref(Date.now())
let ticketCountdownTimer = null

const assignments = ref([])
const assignmentsLoading = ref(false)
const hasMaster = ref(false)
const currentMaster = ref(null)
const pendingUnbindApply = ref(null)
const showApprenticeshipTab = computed(() => hasMaster.value || assignments.value.length > 0)

const voucherDialogVisible = ref(false)
const currentVoucher = ref('')

const openVoucher = (row) => {
  if (row.qrCode) {
    currentVoucher.value = row.qrCode
  } else {
    currentVoucher.value = row.orderNo // Fallback
  }
  voucherDialogVisible.value = true
}

const submitDialogVisible = ref(false)
const submitForm = reactive({
  assignmentId: null,
  status: 0,
  taskTitle: '',
  taskDescription: '',
  demoVideoUrl: '',
  content: '',
  videoUrl: '',
  reviewContent: '',
  score: 0,
  reviewTime: ''
})

const reviewDialogVisible = ref(false)
const currentReview = ref(null)

// Address Logic
const addressList = ref([])
const addressLoading = ref(false)
const addressDialogVisible = ref(false)
const addressForm = reactive({
    id: null,
    receiverName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: false,
    selectedArea: [] // For cascader binding
})
const addressRules = {
    receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号码', trigger: 'blur' }],
    detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
    selectedArea: [{ required: true, message: '请选择省市区', trigger: 'change' }]
}
const addressFormRef = ref(null)

const fetchAddressList = async () => {
    addressLoading.value = true
    try {
        const res = await request.get('/user/address/list')
        if (res.code === 200) {
            addressList.value = res.data || []
        }
    } catch (e) {
        ElMessage.error('加载地址失败')
    } finally {
        addressLoading.value = false
    }
}

const handleAddAddress = () => {
    addressForm.id = null
    addressForm.receiverName = ''
    addressForm.phone = ''
    addressForm.province = ''
    addressForm.city = ''
    addressForm.district = ''
    addressForm.detailAddress = ''
    addressForm.isDefault = false
    addressForm.selectedArea = []
    addressDialogVisible.value = true
}

const handleEditAddress = (row) => {
    Object.assign(addressForm, row)
    // Restore cascader value from text
    // Note: Reverse lookup from text to code is not directly supported by current version of element-china-area-data
    // So we clear the selection if it's an edit, user has to re-select if they want to change area.
    // Or we can try to find codes if we implement search, but for now simple approach:
    addressForm.selectedArea = []
    
    addressDialogVisible.value = true
}

const handleDeleteAddress = (id) => {
    ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            const res = await request.delete(`/user/address/${id}`)
            if (res.code === 200) {
                ElMessage.success('删除成功')
                fetchAddressList()
            }
        } catch (e) {
            ElMessage.error('删除失败')
        }
    })
}

const handleSetDefaultAddress = async (id) => {
    try {
        const res = await request.put(`/user/address/${id}/default`)
        if (res.code === 200) {
            ElMessage.success('设置成功')
            fetchAddressList()
        }
    } catch (e) {
        ElMessage.error('设置失败')
    }
}

const handleAreaChange = (val) => {
    if (val && val.length === 3) {
        addressForm.province = codeToText[val[0]]
        addressForm.city = codeToText[val[1]]
        addressForm.district = codeToText[val[2]]
    }
}

const submitAddress = async () => {
    if (!addressFormRef.value) return
    await addressFormRef.value.validate(async (valid) => {
        if (valid) {
            try {
                if (addressForm.id) {
                    await request.put('/user/address', addressForm)
                    ElMessage.success('更新成功')
                } else {
                    await request.post('/user/address', addressForm)
                    ElMessage.success('添加成功')
                }
                addressDialogVisible.value = false
                fetchAddressList()
            } catch (e) {
                ElMessage.error(e.response?.data?.message || '操作失败')
            }
        }
    })
}



const formatTime = (arr) => {
  if (!arr) return ''
  if (Array.isArray(arr)) {
    const [y, m, d, h, min, s] = arr
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
  }
  return arr
}

const fetchMyOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/mall/my-orders')
    if (res.code === 200) {
      orders.value = res.data || []
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单失败: ' + (e.response?.data?.message || e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const fetchTickets = async () => {
  ticketsLoading.value = true
  try {
    const res = await request.get('/ticket/my-tickets')
    if (res.code === 200) {
      tickets.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载票务失败')
  } finally {
    ticketsLoading.value = false
  }
}

const getTicketCreatedTimestamp = (row) => {
  if (!row?.createdTime) return 0
  const normalized = String(row.createdTime).replace(' ', 'T')
  const timestamp = new Date(normalized).getTime()
  return Number.isNaN(timestamp) ? 0 : timestamp
}

const getTicketRemainingSeconds = (row) => {
  if (row?.status !== 0) return 0
  const expireTs = getTicketCreatedTimestamp(row) + 30 * 1000
  return Math.max(0, Math.ceil((expireTs - ticketNowTs.value) / 1000))
}

const formatTicketCountdown = (row) => {
  const seconds = getTicketRemainingSeconds(row)
  if (seconds <= 0) return '已超时'
  return `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
}

const fetchMyMaster = async () => {
  try {
    const res = await request.get('/master/apprentice/my-master')
    if (res.code === 200 && res.data) {
      hasMaster.value = true
      currentMaster.value = res.data
    } else {
      hasMaster.value = false
      currentMaster.value = null
    }
  } catch (e) {
    hasMaster.value = false
    currentMaster.value = null
  }
}

const fetchPendingUnbindApply = async () => {
  try {
    const res = await request.get('/master/apprentice/unbind/my-apply')
    pendingUnbindApply.value = res.code === 200 ? (res.data || null) : null
  } catch (e) {
    pendingUnbindApply.value = null
  }
}

// Points Logic
const pointsInfo = reactive({
  currentPoints: 0,
  continuousSignDays: 0,
  lastSignDate: null,
  signedIn: false
})
const pointsLogs = ref([])
const pointsLoading = ref(false)
const pointsTotal = ref(0)
const pointsPage = ref(1)

const calculateStreakPercentage = (days) => {
  if (!days) return 0
  if (days >= 30) return 100
  return (days / 30) * 100
}

const streakFormat = (percentage) => {
  return pointsInfo.continuousSignDays + '天'
}

const fetchPointsInfo = async () => {
  if (!user.userId) return
  try {
    const res = await request.get('/points/info', { params: { userId: user.userId } })
    if (res.code === 200) {
      Object.assign(pointsInfo, res.data)
    }
  } catch (e) {
    console.error('Fetch points info failed', e)
  }
}

const fetchPointsLog = async () => {
  if (!user.userId) return
  pointsLoading.value = true
  try {
    const res = await request.get('/points/log', { 
      params: { 
        userId: user.userId,
        page: pointsPage.value,
        size: 10
      } 
    })
    if (res.code === 200) {
      pointsLogs.value = res.data.records
      pointsTotal.value = res.data.total
    }
  } catch (e) {
    console.error('Fetch points log failed', e)
  } finally {
    pointsLoading.value = false
  }
}

const handleSignIn = async () => {
  if (!user.userId) return
  try {
    const res = await request.post('/points/signin', null, { params: { userId: user.userId } })
    if (res.code === 200) {
      const { points, continuousSignDays } = res.data
      ElMessage.success(`签到成功！获得 ${points} 积分，已连续签到 ${continuousSignDays} 天`)
      fetchPointsInfo()
      fetchPointsLog()
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

const handlePointsPageChange = (page) => {
  pointsPage.value = page
  fetchPointsLog()
}

watch(activeTab, (val) => {
  if (val === 'points') {
    fetchPointsInfo()
    fetchPointsLog()
  } else if (val === 'address') {
    fetchAddressList()
  }
})

onMounted(() => {
    const tab = route.query.tab
    if (tab) {
        activeTab.value = tab
    }
    fetchMyOrders()
    fetchTickets()
    fetchMyMaster()
    fetchPendingUnbindApply()
    fetchAssignments()
    // Initial fetch if tab is already set to points or address
    if (activeTab.value === 'points') {
        fetchPointsInfo()
        fetchPointsLog()
    } else if (activeTab.value === 'address') {
        fetchAddressList()
    }
})

const fetchAssignments = async () => {
  assignmentsLoading.value = true
  try {
    const res = await request.get('/teaching/my-assignments')
    if (res.code === 200) {
      assignments.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载作业失败')
  } finally {
    assignmentsLoading.value = false
  }
}

const applyUnbindMaster = async () => {
  if (!hasMaster.value) {
    ElMessage.warning('当前暂无可解除的师父关系')
    return
  }
  if (pendingUnbindApply.value) {
    ElMessage.warning('您已有待审核的解除申请')
    return
  }

  try {
    const { value } = await ElMessageBox.prompt(
      '请输入解除原因。关系解除后，历史任务和资源会保留，可继续查看。',
      '申请解除师父',
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：后续学习方向调整，申请解除当前师徒关系',
        inputValidator: (val) => !!(val && val.trim()),
        inputErrorMessage: '请填写解除原因'
      }
    )

    await request.post('/master/apprentice/unbind/apply', {
      content: value
    })
    ElMessage.success('解除申请已提交，等待师父处理')
    fetchPendingUnbindApply()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '提交失败')
    }
  }
}

const openSubmit = (row) => {
  submitForm.assignmentId = row.assignmentId
  submitForm.status = row.status
  submitForm.taskTitle = row.taskTitle
  submitForm.taskDescription = row.taskDescription
  submitForm.demoVideoUrl = row.demoVideoUrl
  submitForm.content = row.submissionContent || ''
  submitForm.videoUrl = row.submissionVideoUrl || ''
  
  if (row.status === 2) {
      submitForm.reviewContent = row.reviewContent || '暂无评语'
      submitForm.score = (row.score || 0) / 20 // Convert 100 scale to 5 scale
      submitForm.reviewTime = row.reviewTime
  } else {
      submitForm.reviewContent = ''
      submitForm.score = 0
      submitForm.reviewTime = ''
  }
  
  submitDialogVisible.value = true
}

const doSubmit = async () => {
  if (!submitForm.content) {
      ElMessage.warning('请填写作业说明')
      return
  }
  try {
      const res = await request.post('/teaching/submit', {
          assignmentId: submitForm.assignmentId,
          content: submitForm.content,
          videoUrl: submitForm.videoUrl
      })
      if (res.code === 200) {
          ElMessage.success('提交成功')
          submitDialogVisible.value = false
          fetchAssignments()
      } else {
          ElMessage.error(res.message || '提交失败')
      }
  } catch (e) {
      ElMessage.error('提交失败')
  }
}

const handleConfirmReceipt = (row) => {
  ElMessageBox.confirm(
    '确认已收到商品？确认后订单将完成。',
    '收货确认',
    {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'success',
    }
  ).then(async () => {
    try {
      await request.post(`/mall/confirm/${row.id}`)
      ElMessage.success('确认收货成功')
      fetchMyOrders()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleViewLogistics = (row) => {
    if (!row.deliveryCompany && !row.deliveryNo) {
        ElMessage.info('暂无物流信息')
        return
    }
    ElMessageBox.alert(
        `<div><strong>物流公司：</strong>${row.deliveryCompany || '未知'}</div>
         <div style="margin-top:8px"><strong>物流单号：</strong>${row.deliveryNo || '暂无'}</div>`,
        '物流信息',
        {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '关闭'
        }
    )
}

const handleRefund = (row) => {
  ElMessageBox.confirm(
    '确定要申请退票吗？需要等待审核员审核。',
    '退票申请',
    {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await request.post(`/ticket/refund/${row.orderId}`)
        if (res.code === 200) {
          ElMessage.success('申请提交成功，请等待审核')
          fetchTickets()
        } else {
          ElMessage.error(res.message || '申请失败')
        }
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '申请失败')
      }
    })
    .catch(() => {})
}

const goToTicketPay = (row) => {
  router.push({
    path: `/events/${row.eventId}`,
    query: {
      payOrderId: row.orderId
    }
  })
}

const handleMallRefund = (row) => {
  ElMessageBox.confirm(
    '确定要申请取消订单并退款吗？需要等待审核员审核。',
    '退款申请',
    {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await request.post(`/mall/refund/apply/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('申请提交成功')
          fetchMyOrders()
        } else {
          ElMessage.error(res.message || '申请失败')
        }
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '申请失败')
      }
    })
    .catch(() => {})
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    user.avatar = res.data
    ElMessage.success('头像上传成功，请点击保存修改')
  } else {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleHomeworkUploadSuccess = (res) => {
  if (res.code === 200) {
    submitForm.videoUrl = res.data
    ElMessage.success('作业上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const beforeHomeworkUpload = (file) => {
  const isVideoOrAudio = file.type.startsWith('video/') || file.type.startsWith('audio/') || file.type.startsWith('image/')
  const isLt500M = file.size / 1024 / 1024 < 500

  if (!isVideoOrAudio) {
    ElMessage.error('只能上传视频、音频或图片文件!')
  }
  if (!isLt500M) {
    ElMessage.error('文件大小不能超过 500MB!')
  }
  return isVideoOrAudio && isLt500M
}

const isImage = (url) => {
  if (!url) return false
  return /\.(jpeg|jpg|gif|png|webp)$/i.test(url)
}

const saveProfile = async () => {
  if (!user.userId) {
    ElMessage.error('用户信息丢失，请重新登录')
    return
  }
  try {
    await request.put(`/users/${user.userId}`, {
      realName: user.realName,
      phone: user.phone,
      email: user.email,
      avatar: user.avatar
    })
    ElMessage.success('保存成功')
    // 更新本地缓存
    localStorage.setItem('user', JSON.stringify(user))
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchMyMaster()
  const userStr = localStorage.getItem('user')
  if (userStr) {
    Object.assign(user, JSON.parse(userStr))
  }
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
  // Initial fetch based on active tab
  if (activeTab.value === 'orders') {
    fetchMyOrders()
  } else if (activeTab.value === 'tickets') {
    fetchTickets()
  } else if (activeTab.value === 'apprenticeship') {
    fetchAssignments()
  } else if (activeTab.value === 'points') {
    fetchPointsInfo()
    fetchPointsLog()
  }
  ticketCountdownTimer = window.setInterval(() => {
    ticketNowTs.value = Date.now()
    if (activeTab.value === 'tickets' && tickets.value.some(item => item.status === 0 && getTicketRemainingSeconds(item) <= 0)) {
      fetchTickets()
    }
  }, 1000)
})

onUnmounted(() => {
  if (ticketCountdownTimer) {
    window.clearInterval(ticketCountdownTimer)
    ticketCountdownTimer = null
  }
})

// Combined watch is better, but separate is fine.
// The previous tool added a watch for 'points'. 
// I will just add styles now.

watch(activeTab, (val) => {
  if (val === 'orders') {
    fetchMyOrders()
  } else if (val === 'tickets') {
    fetchTickets()
  } else if (val === 'apprenticeship') {
    fetchAssignments()
  }
})
</script>

<style scoped>
.page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.ticket-countdown {
  color: #e6a23c;
  font-weight: 600;
}
.avatar-section {
  text-align: center;
  margin-bottom: 20px;
}
.username {
  text-align: center;
  margin-bottom: 20px;
}
.role-badge {
  margin-top: 10px;
}
.info-list p {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  margin-bottom: 8px;
}
.points-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}
.current-points {
  text-align: center;
}
.current-points .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.current-points .value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}
.sign-in-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.streak-info {
  margin-top: 10px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}
.streak-info .highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}
.streak-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.plus-point {
  color: #67c23a;
  font-weight: bold;
}
.minus-point {
  color: #f56c6c;
  font-weight: bold;
}
.actions {
  margin-top: 30px;
}
.user-card {
  height: 100%;
}
.content-card {
  min-height: 500px;
}
.apprenticeship-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.pending-unbind-alert {
  margin-bottom: 16px;
}
.order-info-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.5;
}
.order-no {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}
.create-time {
  font-size: 12px;
  color: #909399;
}
.product-item-flex {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  cursor: pointer;
  transition: opacity 0.2s;
}
.product-item-flex:hover {
  opacity: 0.8;
}
.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  background: #f5f7fa;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #dcdfe6;
}
.product-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.product-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-qty {
  font-size: 12px;
  color: #909399;
}
.price-text {
  font-weight: 600;
  color: #303133;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
  font-size: 13px;
}
.text-gray {
  color: #606266;
}
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-edit {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 4px;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}
.avatar-uploader-icon:hover {
  border-color: #409EFF;
}
</style>
