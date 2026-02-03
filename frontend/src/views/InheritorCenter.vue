<template>
  <div class="page">
    <el-tabs v-model="activeTab" type="border-card">
      
      <!-- 1. 档案管理 -->
      <el-tab-pane label="我的档案" name="profile">
        <div class="form-container">
          <h3>传承人档案</h3>
          <el-form :model="profileForm" label-width="100px">
            <el-form-item label="等级">
              <el-input v-model="profileForm.level" disabled />
            </el-form-item>
            <el-form-item label="师承">
              <el-input v-model="profileForm.masterName" :disabled="!!profileForm.masterId" placeholder="未绑定师父，可手动填写" />
            </el-form-item>
            <el-form-item label="流派">
              <el-input v-model="profileForm.genre" />
            </el-form-item>
            <el-form-item label="获奖情况">
              <el-input v-model="profileForm.awards" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="演艺经历">
              <el-input v-model="profileForm.artisticCareer" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="updateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 2. 师门管理 -->
      <el-tab-pane label="师门管理" name="apprenticeship">
        <div class="toolbar">
           <el-alert title="仅显示待审核的拜师申请" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />
        </div>
        
        <div v-if="apprenticeshipList.length === 0" class="empty-text">暂无待审核申请</div>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in apprenticeshipList" :key="item.id">
            <el-card shadow="hover" class="apprentice-card">
              <div class="student-header">
                <el-avatar :size="50" :src="item.studentAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                <div class="student-info">
                  <div class="name">{{ item.studentName }}</div>
                  <div class="id">ID: {{ item.studentId }}</div>
                </div>
              </div>
              <div class="apply-content">
                <p><strong>拜师贴：</strong></p>
                <div class="content-text">{{ item.applyContent }}</div>
              </div>
              <div class="actions">
                <el-button type="success" size="small" @click="openAuditDialog(item, 1)">通过</el-button>
                <el-button type="danger" size="small" @click="openAuditDialog(item, 2)">拒绝</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 8. My Apprentices -->
      <el-tab-pane label="我的门徒" name="my-apprentices">
        <el-table :data="myApprenticesList" v-loading="myApprenticesLoading" style="width: 100%">
            <el-table-column label="头像" width="80">
                <template #default="scope">
                    <el-avatar :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                </template>
            </el-table-column>
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="phone" label="电话" />
            <el-table-column prop="level" label="等级" />
            <el-table-column prop="joinTime" label="拜师时间">
                <template #default="scope">{{ formatDate(scope.row.joinTime) }}</template>
            </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 7. 授业管理 (Teaching) -->
      <el-tab-pane label="授业管理" name="teaching">
        <div class="toolbar">
           <el-button type="primary" @click="openPublishDialog">发布新任务</el-button>
        </div>
        
        <el-table :data="tasksList" style="width: 100%; margin-top: 20px;" v-loading="tasksLoading">
          <el-table-column prop="title" label="任务标题" />
          <el-table-column prop="createdTime" label="发布时间">
             <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="openSubmissionDrawer(scope.row)">查看作业</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 9. 订单管理 -->
      <el-tab-pane label="订单管理" name="order-mgmt">
        <div class="toolbar">
           <el-button @click="fetchSellerOrders" :icon="Refresh">刷新订单</el-button>
        </div>
        
        <el-table :data="sellerOrdersList" v-loading="sellerOrdersLoading" style="width: 100%; margin-top: 20px;">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="createTime" label="下单时间" width="160" />
          <el-table-column label="包含商品" width="300">
             <template #default="scope">
                <div v-for="item in scope.row.items" :key="item.id" class="order-item-mini">
                    <el-image :src="item.productImage" style="width: 40px; height: 40px; margin-right: 10px;" />
                    <span class="item-name">{{ item.productName }} x {{ item.quantity }}</span>
                </div>
             </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="订单总额" width="120">
              <template #default="scope">¥{{ scope.row.totalAmount }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
             <template #default="scope">
                <el-tag :type="getOrderStatusType(scope.row.status)">{{ getOrderStatusText(scope.row.status) }}</el-tag>
             </template>
          </el-table-column>
          <el-table-column label="收货信息" width="200" show-overflow-tooltip>
             <template #default="scope">{{ scope.row.addressSnapshot }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="180">
            <template #default="scope">
               <el-button v-if="scope.row.status === 1" type="primary" size="small" @click="openShipDialog(scope.row)">发货</el-button>
               <el-button v-if="scope.row.status === 1 || scope.row.status === 4" type="danger" size="small" @click="handleRefund(scope.row)">退款/取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 3. 资源管理 -->
      <el-tab-pane label="资源管理" name="resources">
        <div class="toolbar">
          <el-button type="primary" @click="openResourceDialog()">上传新资源</el-button>
        </div>
        <el-table :data="resourceList" style="width: 100%; margin-top: 20px;" v-loading="resourceListLoading">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="type" label="类型" width="100">
             <template #default="scope">
               {{ getResourceTypeLabel(scope.row.type) }}
             </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="上传时间" width="180">
              <template #default="scope">
                  {{ formatDate(scope.row.createdTime) }}
              </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="openResourceDialog(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="deleteResource(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <!-- 4. 文创管理 -->
      <el-tab-pane label="文创管理" name="products">
         <div class="toolbar">
          <el-button type="primary" @click="openProductDialog()">发布新商品</el-button>
        </div>
        <el-table :data="productList" style="width: 100%; margin-top: 20px;" v-loading="productListLoading">
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="price" label="价格" width="100">
             <template #default="scope">¥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="100">
             <template #default="scope">
                <span v-if="scope.row.stock === 0" style="color: red; font-weight: bold;">无货</span>
                <span v-else>{{ scope.row.stock }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
           <el-table-column prop="createdTime" label="创建时间" width="180">
              <template #default="scope">
                  {{ formatDate(scope.row.createdTime) }}
              </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="openProductDialog(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="deleteProduct(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 5. 活动管理 -->
      <el-tab-pane label="活动管理" name="activity">
        <div class="toolbar">
           <el-button type="primary" @click="showActivityDialog">发布新活动</el-button>
        </div>
        <el-table :data="activities" style="width: 100%; margin-top: 20px;" v-loading="activityLoading">
          <el-table-column prop="title" label="活动标题" />
          <el-table-column prop="showTime" label="时间">
              <template #default="scope">
                  {{ formatDate(scope.row.showTime) }}
              </template>
          </el-table-column>
          <el-table-column prop="venue" label="地点" />
          <el-table-column prop="status" label="状态">
             <template #default="scope">
               <el-tag :type="getStatusType(scope.row.status, scope.row.showTime)">
                 {{ getStatusLabel(scope.row.status, scope.row.showTime) }}
               </el-tag>
             </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="showActivityDialog(scope.row)">编辑</el-button>
              <el-button v-if="scope.row.status === 1 && !isExpired(scope.row.showTime)" link type="warning" @click="handleOffline(scope.row)">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <!-- 6. 销售数据 -->
      <el-tab-pane label="文创销售数据" name="sales">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>总销售额</template>
              <div class="stat-num">¥ {{ salesStats.totalSales || '0.00' }}</div>
            </el-card>
          </el-col>
           <el-col :span="8">
            <el-card shadow="hover">
              <template #header>订单总数</template>
              <div class="stat-num">{{ salesStats.orderCount || 0 }}</div>
            </el-card>
          </el-col>
           <el-col :span="8">
            <el-card shadow="hover">
              <template #header>在售商品</template>
              <div class="stat-num">{{ salesStats.productCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
        
        <div class="sales-tips" style="margin-top: 20px; color: #666;">
          <p>* 数据实时更新，仅统计已完成支付的订单。</p>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Teaching: Publish Task Dialog -->
    <el-dialog v-model="publishDialogVisible" title="发布任务" width="500px">
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="publishForm.title" placeholder="如：拉魂腔经典选段练习" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="publishForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="指定弟子">
           <el-select v-model="publishForm.studentIds" multiple placeholder="请选择弟子" style="width: 100%">
             <el-option v-for="s in myApprentices" :key="s.studentId" :label="s.name" :value="s.studentId" />
           </el-select>
        </el-form-item>
        <el-form-item label="示范视频">
           <el-upload
              class="upload-demo"
              action="/api/file/upload"
              :limit="1"
              :on-success="handleTaskVideoSuccess"
              :before-upload="beforeUpload">
              <el-button type="primary">上传视频</el-button>
              <template #tip>
                <div class="el-upload__tip">{{ publishForm.videoUrl ? '已上传' : '支持mp4等格式' }}</div>
              </template>
            </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishSubmitting" @click="submitTask">发布</el-button>
      </template>
    </el-dialog>

    <!-- Teaching: Submissions Drawer -->
    <el-drawer v-model="submissionDrawerVisible" title="作业提交情况" size="50%">
      <el-table :data="currentTaskSubmissions">
        <el-table-column prop="studentName" label="弟子" width="120" />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
             <el-tag :type="scope.row.status === 0 ? 'info' : (scope.row.status === 1 ? 'primary' : 'success')">
               {{ scope.row.status === 0 ? '待提交' : (scope.row.status === 1 ? '已提交' : '已点评') }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间">
            <template #default="scope">{{ formatDate(scope.row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作">
           <template #default="scope">
             <el-button v-if="scope.row.status > 0" size="small" type="primary" @click="openReviewDialog(scope.row)">
               {{ scope.row.status === 1 ? '点评' : '查看点评' }}
             </el-button>
           </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- Teaching: Review Dialog -->
    <el-dialog v-model="commentDialogVisible" title="作业点评" width="500px">
      <div class="submission-preview" v-if="currentReviewItem">
         <p><strong>学生留言：</strong> {{ currentReviewItem.submissionContent }}</p>
         <div v-if="currentReviewItem.submissionVideoUrl">
            <video :src="currentReviewItem.submissionVideoUrl" controls width="100%" style="margin-top: 10px; max-height: 200px;"></video>
         </div>
      </div>
      <el-divider>历史评论</el-divider>
      <div class="comments-list" style="max-height: 300px; overflow-y: auto; margin-bottom: 20px;">
          <div v-for="c in submissionComments" :key="c.commentId" 
               class="comment-item" 
               :class="{ 'official-comment': c.isOfficial === 1 }">
              <div class="comment-header">
                  <span class="comment-author">{{ c.userName }}</span>
                  <el-tag v-if="c.isOfficial === 1" type="warning" effect="dark" size="small" class="official-badge">恩师点评</el-tag>
                  <span class="comment-time">{{ c.createdTime }}</span>
              </div>
              <div class="comment-content">{{ c.content }}</div>
          </div>
          <div v-if="submissionComments.length === 0" style="text-align: center; color: #999;">暂无评论</div>
      </div>
      
      <el-form>
         <el-form-item>
           <el-input v-model="newComment" type="textarea" :rows="2" placeholder="写下您的点评..." />
         </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commentDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="submitReview">发表点评</el-button>
      </template>
    </el-dialog>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditDialogVisible" :title="auditAction === 1 ? '通过申请' : '拒绝申请'" width="400px">
      <el-form>
         <el-form-item label="寄语/理由">
           <el-input v-model="auditMessage" type="textarea" :rows="3" placeholder="请输入寄语或拒绝理由..." />
         </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>

    <!-- Resource Dialog -->
    <el-dialog v-model="resourceDialogVisible" :title="resourceForm.id ? '编辑资源' : '上传资源'">
      <el-form :model="resourceForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="resourceForm.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="resourceForm.type">
            <el-option label="视频" :value="1" />
            <el-option label="音频" :value="2" />
            <el-option label="图文" :value="3" />
            <el-option label="剧本PDF" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="resourceForm.category" placeholder="如：经典剧目、拉魂腔" />
        </el-form-item>
        <el-form-item label="封面">
           <el-upload
              class="avatar-uploader"
              action="/api/file/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeUpload">
              <img v-if="resourceForm.coverImg" :src="resourceForm.coverImg" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
        </el-form-item>
        <el-form-item label="资源文件">
           <el-upload
              class="upload-demo"
              action="/api/file/upload"
              :limit="1"
              :on-success="handleFileSuccess"
              :before-upload="beforeUpload">
              <el-button type="primary">点击上传</el-button>
              <template #tip>
                <div class="el-upload__tip">
                   {{ resourceForm.fileUrl ? '已上传: ' + resourceForm.fileUrl : '支持视频/音频/PDF等格式' }}
                </div>
              </template>
            </el-upload>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="resourceForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resourceSubmitting" @click="submitResource">提交</el-button>
      </template>
    </el-dialog>

    <!-- Product Dialog -->
    <el-dialog v-model="productDialogVisible" :title="productForm.id ? '编辑商品' : '发布商品'">
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="productForm.subTitle" />
        </el-form-item>
        <el-form-item label="主图">
           <el-upload
              class="avatar-uploader"
              action="/api/file/upload"
              :show-file-list="false"
              :on-success="handleProductImageSuccess"
              :before-upload="beforeUpload">
              <img v-if="productForm.mainImage" :src="productForm.mainImage" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
        </el-form-item>
        <el-form-item label="价格">
           <el-input-number v-model="productForm.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="库存">
           <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="详情(HTML)">
          <el-input v-model="productForm.detail" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="productSubmitting" @click="submitProduct">提交</el-button>
      </template>
    </el-dialog>

    <!-- Activity Dialog (Existing) -->
    <el-dialog v-model="dialogVisible" title="发布活动">
      <el-form :model="activityForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="activityForm.title" />
        </el-form-item>
        <el-form-item label="时间">
           <el-date-picker v-model="activityForm.showTime" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="封面图片">
           <el-upload
              class="avatar-uploader"
              action="/api/file/upload"
              :show-file-list="false"
              :on-success="handleActivityCoverSuccess"
              :before-upload="beforeUpload">
              <img v-if="activityForm.coverImage" :src="activityForm.coverImage" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="activityForm.venue" />
        </el-form-item>
        <el-form-item label="活动介绍">
          <el-input v-model="activityForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number v-model="activityForm.ticketPrice" :min="0" />
        </el-form-item>
        <el-form-item label="座位布局">
           <el-button type="primary" plain @click="openSeatEditor">
             {{ activityForm.seatLayoutJson && activityForm.seatLayoutJson.length > 5 ? '修改座位布局' : '去设置座位布局' }}
           </el-button>
           <span style="margin-left: 10px; color: #666">
             当前座位数: {{ activityForm.totalSeats }}
           </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="activitySubmitting" @click="submitActivity">确认发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="seatDialogVisible" title="座位布局设置" width="80%" append-to-body>
        <SeatEditor v-model="currentSeatLayout" />
        <template #footer>
            <el-button @click="seatDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmSeatLayout">确认保存</el-button>
        </template>
    </el-dialog>

    <!-- Ritual Animation Overlay -->
    <div v-if="ritualVisible" class="ritual-overlay">
      <div class="ritual-content">
        <div class="tea-cup-container">
           <svg class="tea-cup" viewBox="0 0 100 100">
              <defs>
                <linearGradient id="steamGradient" x1="0%" y1="100%" x2="0%" y2="0%">
                  <stop offset="0%" style="stop-color:white;stop-opacity:0" />
                  <stop offset="50%" style="stop-color:white;stop-opacity:0.6" />
                  <stop offset="100%" style="stop-color:white;stop-opacity:0" />
                </linearGradient>
              </defs>
              <path d="M20,40 Q20,80 50,80 Q80,80 80,40 L80,30 L20,30 Z" fill="#D2691E" stroke="#8B4513" stroke-width="2" />
              <path d="M80,40 Q95,40 95,55 Q95,70 80,70" fill="none" stroke="#D2691E" stroke-width="4" />
              <path d="M25,35 Q50,35 75,35 Q75,40 50,40 Q25,40 25,35" fill="#8B4513" />
              <path class="steam steam-1" d="M40,25 Q35,15 40,5" stroke="url(#steamGradient)" fill="none" stroke-width="3" />
              <path class="steam steam-2" d="M50,25 Q55,15 50,5" stroke="url(#steamGradient)" fill="none" stroke-width="3" />
              <path class="steam steam-3" d="M60,25 Q55,15 60,5" stroke="url(#steamGradient)" fill="none" stroke-width="3" />
           </svg>
        </div>
        <div class="ritual-text">
            <h2>敬茶礼成</h2>
            <p>薪火相传，生生不息</p>
        </div>
      </div>
    </div>
    <!-- Ship Dialog -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="400px">
       <el-form :model="shipForm" label-width="80px">
          <el-form-item label="快递公司">
             <el-input v-model="shipForm.deliveryCompany" placeholder="如：顺丰速运" />
          </el-form-item>
          <el-form-item label="快递单号">
             <el-input v-model="shipForm.deliveryNo" placeholder="请输入快递单号" />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmShip">确认发货</el-button>
       </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import request from '../utils/request'
import SeatEditor from '../components/SeatEditor.vue'

const activeTab = ref('profile')

// --- Utils ---
const formatDate = (dateStr) => {
    if (!dateStr) return ''
    if (typeof dateStr === 'string') {
        return dateStr.replace('T', ' ').substring(0, 19)
    }
    return dateStr
}

const isExpired = (timeStr) => {
    if (!timeStr) return false
    return new Date(timeStr).getTime() < new Date().getTime()
}

const getStatusLabel = (status, showTime) => {
    if (showTime && isExpired(showTime)) return '已结束'
    if (status === 0) return '审核中'
    if (status === 1) return '已上架'
    if (status === 2) return '未通过'
    if (status === 3) return '已下架'
    return '未知'
}

const getStatusType = (status, showTime) => {
    if (showTime && isExpired(showTime)) return 'info'
    if (status === 0) return 'warning'
    if (status === 1) return 'success'
    if (status === 2) return 'danger'
    if (status === 3) return 'info'
    return 'info'
}

const getResourceTypeLabel = (type) => {
    const map = {1: '视频', 2: '音频', 3: '图文', 4: '剧本PDF'}
    return map[type] || '未知'
}

// --- 1. Profile ---
const profileForm = reactive({
  id: null,
  userId: null,
  masterId: null,
  level: '',
  masterName: '',
  genre: '',
  awards: '',
  artisticCareer: ''
})
const profileLoading = ref(false)

const fetchProfile = async () => {
  try {
    const res = await request.get('/inheritor/profile')
    if (res.data) {
      Object.assign(profileForm, res.data)
    }
  } catch (e) {
    // ignore
  }
}

const updateProfile = async () => {
  profileLoading.value = true
  try {
    await request.put('/inheritor/profile', profileForm)
    ElMessage.success('档案更新成功')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '更新失败')
  } finally {
    profileLoading.value = false
  }
}

// --- 2. Apprenticeship ---
const apprenticeshipList = ref([])
const auditDialogVisible = ref(false)
const auditAction = ref(1) // 1-pass, 2-reject
const auditMessage = ref('')
const currentAuditItem = ref(null)
const ritualVisible = ref(false)

const fetchApprenticeship = async () => {
    try {
        const res = await request.get('/master/apprentice/list')
        apprenticeshipList.value = res.data?.records || []
    } catch (e) {
        // ignore
    }
}

const openAuditDialog = (item, action) => {
    currentAuditItem.value = item
    auditAction.value = action
    auditMessage.value = ''
    auditDialogVisible.value = true
}

const submitAudit = async () => {
    if (!currentAuditItem.value) return
    try {
        await request.put('/master/apprentice/audit', {
            id: currentAuditItem.value.id,
            status: auditAction.value,
            message: auditMessage.value
        })
        
        auditDialogVisible.value = false
        
        if (auditAction.value === 1) {
             // Show Ritual Animation
             ritualVisible.value = true
             setTimeout(() => {
                 ritualVisible.value = false
                 ElMessage.success('操作成功，已发布社区动态')
                 fetchApprenticeship()
             }, 4000) // 4 seconds animation
        } else {
             ElMessage.success('已拒绝申请')
             fetchApprenticeship()
        }
    } catch (e) {
        ElMessage.error(e?.response?.data?.message || '操作失败')
    }
}

// --- 3. Resources ---
const resourceList = ref([])
const resourceListLoading = ref(false)
const resourceDialogVisible = ref(false)
const resourceSubmitting = ref(false)
const resourceForm = reactive({
  id: null,
  title: '',
  type: 1,
  category: '',
  coverImg: '',
  fileUrl: '',
  description: '',
})

const fetchResources = async () => {
    resourceListLoading.value = true
    try {
        const res = await request.get('/resources/my-resources')
        resourceList.value = res.data || []
    } catch (e) {
        // ignore
    } finally {
        resourceListLoading.value = false
    }
}

const openResourceDialog = (item = null) => {
    if (item) {
        resourceForm.id = item.resourceId
        resourceForm.title = item.title
        resourceForm.type = item.type
        resourceForm.category = item.category
        resourceForm.coverImg = item.coverImg
        resourceForm.fileUrl = item.fileUrl
        resourceForm.description = item.description
    } else {
        resourceForm.id = null
        resourceForm.title = ''
        resourceForm.type = 1
        resourceForm.category = ''
        resourceForm.coverImg = ''
        resourceForm.fileUrl = ''
        resourceForm.description = ''
    }
    resourceDialogVisible.value = true
}

const submitResource = async () => {
  resourceSubmitting.value = true
  try {
    if (resourceForm.id) {
        await request.put(`/resources/my-resource/${resourceForm.id}`, resourceForm)
        ElMessage.success('更新成功，已重新提交审核')
    } else {
        await request.post('/resources', resourceForm)
        ElMessage.success('上传成功，请等待管理员审核')
    }
    resourceDialogVisible.value = false
    fetchResources()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    resourceSubmitting.value = false
  }
}

const deleteResource = async (item) => {
    try {
        await ElMessageBox.confirm('确定要删除该资源吗?', '提示', { type: 'warning' })
        await request.delete(`/resources/my-resource/${item.resourceId}`)
        ElMessage.success('删除成功')
        fetchResources()
    } catch (e) {
        // ignore
    }
}

const handleCoverSuccess = (res) => {
    if (res.code === 200) resourceForm.coverImg = res.data
}

const handleFileSuccess = (res) => {
    if (res.code === 200) resourceForm.fileUrl = res.data
}

// --- 4. Products ---
const productList = ref([])
const productListLoading = ref(false)
const productDialogVisible = ref(false)
const productSubmitting = ref(false)
const productForm = reactive({
    id: null,
    name: '',
    subTitle: '',
    mainImage: '',
    price: 0,
    stock: 0,
    detail: ''
})

const fetchProducts = async () => {
    productListLoading.value = true
    try {
        const res = await request.get('/products/my-products')
        productList.value = res.data || []
    } catch (e) {
        // ignore
    } finally {
        productListLoading.value = false
    }
}

const openProductDialog = (item = null) => {
    if (item) {
        productForm.id = item.productId
        productForm.name = item.name
        productForm.subTitle = item.subTitle
        productForm.mainImage = item.mainImage
        productForm.price = item.price
        productForm.stock = item.stock
        productForm.detail = item.detail
    } else {
        productForm.id = null
        productForm.name = ''
        productForm.subTitle = ''
        productForm.mainImage = ''
        productForm.price = 0
        productForm.stock = 0
        productForm.detail = ''
    }
    productDialogVisible.value = true
}

const submitProduct = async () => {
    productSubmitting.value = true
    try {
        if (productForm.id) {
            await request.put(`/products/my-product/${productForm.id}`, productForm)
             ElMessage.success('更新成功，已重新提交审核')
        } else {
            await request.post('/products/create', productForm)
             ElMessage.success('发布成功，请等待管理员审核')
        }
        productDialogVisible.value = false
        fetchProducts()
    } catch (e) {
        ElMessage.error(e?.response?.data?.message || '提交失败')
    } finally {
        productSubmitting.value = false
    }
}

const deleteProduct = async (item) => {
     try {
        await ElMessageBox.confirm('确定要删除该商品吗?', '提示', { type: 'warning' })
        await request.delete(`/products/my-product/${item.productId}`)
        ElMessage.success('删除成功')
        fetchProducts()
    } catch (e) {
        // ignore
    }
}

const handleProductImageSuccess = (res) => {
    if (res.code === 200) productForm.mainImage = res.data
}

// --- 5. Activities (Existing logic) ---
const activities = ref([])
const activityLoading = ref(false)
const dialogVisible = ref(false)
const activitySubmitting = ref(false)
const activityForm = reactive({
  eventId: null,
  title: '',
  showTime: '',
  venue: '',
  ticketPrice: 0,
  totalSeats: 100,
  seatLayoutJson: '[]',
  description: '',
  coverImage: ''
})

const seatDialogVisible = ref(false)
const currentSeatLayout = ref([])

const openSeatEditor = async () => {
    if (activityForm.seatLayoutJson && typeof activityForm.seatLayoutJson === 'string' && activityForm.seatLayoutJson.length > 5) {
        try {
            currentSeatLayout.value = JSON.parse(activityForm.seatLayoutJson)
        } catch (e) {
            currentSeatLayout.value = []
        }
    } else {
        currentSeatLayout.value = []
    }
    await nextTick()
    seatDialogVisible.value = true
}

const confirmSeatLayout = () => {
    activityForm.seatLayoutJson = JSON.stringify(currentSeatLayout.value)
    activityForm.totalSeats = currentSeatLayout.value.length
    seatDialogVisible.value = false
    ElMessage.success('座位布局已保存')
}

const fetchActivities = async () => {
    activityLoading.value = true
    try {
        const res = await request.get('/events/my-events')
        activities.value = res.data || []
    } catch (e) {
        ElMessage.error('获取活动列表失败')
    } finally {
        activityLoading.value = false
    }
}

const showActivityDialog = (row = null) => {
    if (row) {
        activityForm.eventId = row.eventId
        activityForm.title = row.title
        activityForm.showTime = row.showTime
        activityForm.venue = row.venue
        activityForm.ticketPrice = row.ticketPrice
        activityForm.totalSeats = row.totalSeats
        activityForm.seatLayoutJson = row.seatLayoutJson || '[]'
    } else {
        activityForm.eventId = null
        activityForm.title = ''
        activityForm.showTime = ''
        activityForm.venue = ''
        activityForm.ticketPrice = 0
        activityForm.totalSeats = 100
        activityForm.seatLayoutJson = '[]'
    }
    dialogVisible.value = true
}

const handleActivityCoverSuccess = (res) => {
    // console.log('Upload response:', res) // Debugging
    if (res.code === 200) {
        activityForm.coverImage = res.data
        ElMessage.success('封面上传成功')
    } else {
        ElMessage.error(res.message || '上传失败')
    }
}

const submitActivity = async () => {
    if (!activityForm.title || !activityForm.showTime || !activityForm.venue) {
        ElMessage.warning('请填写完整信息')
        return
    }
    activitySubmitting.value = true
    try {
        if (activityForm.eventId) {
            // Ensure ID is passed for update
            await request.put('/events/update', { ...activityForm })
            ElMessage.success('更新成功')
        } else {
            await request.post('/events/create', activityForm)
            ElMessage.success('发布成功，请等待管理员审核')
        }
        dialogVisible.value = false
        fetchActivities()
    } catch (e) {
        ElMessage.error(e.response?.data?.message || '操作失败')
    } finally {
        activitySubmitting.value = false
    }
}

const handleOffline = async (row) => {
    try {
        await ElMessageBox.confirm('确定要下架该活动吗？下架后将不再展示给用户。', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await request.post(`/events/offline/${row.eventId}`)
        ElMessage.success('下架成功')
        fetchActivities()
    } catch (e) {
        if (e !== 'cancel') {
             ElMessage.error(e.response?.data?.message || '操作失败')
        }
    }
}

// --- 6. Sales ---
const salesStats = reactive({
    totalSales: '0.00',
    orderCount: 0,
    productCount: 0
})

const fetchSalesStats = async () => {
    try {
        const res = await request.get('/products/my-sales-stats')
        if (res.data) {
            salesStats.totalSales = res.data.totalSales || '0.00'
            salesStats.orderCount = res.data.orderCount || 0
            salesStats.productCount = res.data.productCount || 0
        }
    } catch (e) {
        console.error('Failed to fetch sales stats', e)
    }
}

// --- 7. Teaching (授业) ---
const tasksList = ref([])
const tasksLoading = ref(false)
const publishDialogVisible = ref(false)
const publishSubmitting = ref(false)
const publishForm = reactive({
    title: '',
    description: '',
    videoUrl: '',
    studentIds: []
})
const myApprentices = ref([])
const submissionDrawerVisible = ref(false)
const currentTaskSubmissions = ref([])
const commentDialogVisible = ref(false)
const submissionComments = ref([])
const newComment = ref('')
const currentReviewItem = ref(null)
const currentAssignmentId = ref(null)

const fetchTasks = async () => {
    tasksLoading.value = true
    try {
        const res = await request.get('/teaching/tasks')
        tasksList.value = res.data || []
    } catch (e) {
        // ignore
    } finally {
        tasksLoading.value = false
    }
}

const openPublishDialog = async () => {
    // 1. Fetch apprentices
    try {
        const res = await request.get('/teaching/my-apprentices')
        myApprentices.value = res.data || []
    } catch (e) {
        ElMessage.error('获取弟子列表失败')
        return
    }
    
    // 2. Reset form
    publishForm.title = ''
    publishForm.description = ''
    publishForm.videoUrl = ''
    publishForm.studentIds = []
    
    publishDialogVisible.value = true
}

const handleTaskVideoSuccess = (res) => {
    if (res.code === 200) publishForm.videoUrl = res.data
}

const submitTask = async () => {
    if (!publishForm.title) {
        ElMessage.warning('请输入任务标题')
        return
    }
    if (publishForm.studentIds.length === 0) {
        ElMessage.warning('请选择至少一名弟子')
        return
    }
    
    publishSubmitting.value = true
    try {
        await request.post('/teaching/task', publishForm)
        ElMessage.success('任务发布成功')
        publishDialogVisible.value = false
        fetchTasks()
    } catch (e) {
        ElMessage.error(e?.response?.data?.message || '发布失败')
    } finally {
        publishSubmitting.value = false
    }
}

const openSubmissionDrawer = async (task) => {
    submissionDrawerVisible.value = true
    currentTaskSubmissions.value = []
    try {
        const res = await request.get(`/teaching/task/${task.id}/submissions`)
        currentTaskSubmissions.value = res.data || []
    } catch (e) {
        ElMessage.error('获取提交情况失败')
    }
}

const openReviewDialog = async (assignment) => {
    currentReviewItem.value = assignment
    currentAssignmentId.value = assignment.id
    newComment.value = ''
    commentDialogVisible.value = true
    
    // Fetch comments
    submissionComments.value = []
    try {
        const res = await request.get(`/teaching/assignment/${assignment.id}/comments`)
        submissionComments.value = res.data || []
    } catch (e) {
        // ignore
    }
}

const submitReview = async () => {
    if (!newComment.value) {
        ElMessage.warning('请输入点评内容')
        return
    }
    
    try {
        await request.post('/teaching/review', {
            assignmentId: currentAssignmentId.value,
            comment: newComment.value
        })
        ElMessage.success('点评成功')
        newComment.value = ''
        // Refresh comments
        const res = await request.get(`/teaching/assignment/${currentAssignmentId.value}/comments`)
        submissionComments.value = res.data || []
        
        // Update status in list locally
        if (currentReviewItem.value) {
            currentReviewItem.value.status = 2 // Reviewed
        }
    } catch (e) {
        ElMessage.error(e?.response?.data?.message || '点评失败')
    }
}

// --- My Apprentices (Full Info) ---
const myApprenticesList = ref([])
const myApprenticesLoading = ref(false)

const fetchMyApprenticesFull = async () => {
    myApprenticesLoading.value = true
    try {
        const res = await request.get('/master/apprentice/my-apprentices')
        myApprenticesList.value = res.data || []
    } catch (e) {
        ElMessage.error('获取徒弟列表失败')
    } finally {
        myApprenticesLoading.value = false
    }
}

watch(activeTab, (val) => {
    if (val === 'teaching') {
        fetchTasks()
    } else if (val === 'apprenticeship') {
        fetchApprenticeship()
    } else if (val === 'my-apprentices') {
        fetchMyApprenticesFull()
    } else if (val === 'resources') {
        fetchResources()
    } else if (val === 'products') {
        fetchProducts()
    } else if (val === 'activity') {
        fetchActivities()
    } else if (val === 'sales') {
        fetchSalesStats()
    }
})

// --- Upload Helper ---
const beforeUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过 50MB!')
  }
  return isLt50M
}

// --- Init ---
// --- Order Management ---
const sellerOrdersList = ref([])
const sellerOrdersLoading = ref(false)
const shipDialogVisible = ref(false)
const shipForm = reactive({
    orderId: null,
    deliveryCompany: '',
    deliveryNo: ''
})

const fetchSellerOrders = async () => {
    sellerOrdersLoading.value = true
    try {
        const res = await request.get('/seller/orders')
        sellerOrdersList.value = res.data || []
    } catch(e) {
        ElMessage.error('获取订单列表失败')
    } finally {
        sellerOrdersLoading.value = false
    }
}

const getOrderStatusText = (status) => {
    const map = {0:'待支付', 1:'待发货', 2:'已发货', 3:'已取消/退款', 4:'退款申请中', 5:'已退款'}
    return map[status] || '未知'
}
const getOrderStatusType = (status) => {
    const map = {0:'warning', 1:'primary', 2:'success', 3:'info', 4:'danger', 5:'info'}
    return map[status] || 'info'
}

const openShipDialog = (row) => {
    shipForm.orderId = row.id
    shipForm.deliveryCompany = ''
    shipForm.deliveryNo = ''
    shipDialogVisible.value = true
}

const confirmShip = async () => {
    if(!shipForm.deliveryNo) {
        ElMessage.warning('请输入快递单号')
        return
    }
    try {
        await request.post(`/seller/orders/ship/${shipForm.orderId}`, null, {
            params: {
                deliveryCompany: shipForm.deliveryCompany,
                deliveryNo: shipForm.deliveryNo
            }
        })
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        fetchSellerOrders()
    } catch(e) {
        ElMessage.error(e.response?.data?.message || '发货失败')
    }
}

const handleRefund = (row) => {
    ElMessageBox.confirm('确定要退款/取消该订单吗？库存将自动回滚。', '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            await request.post(`/seller/orders/refund/${row.id}`)
            ElMessage.success('操作成功')
            fetchSellerOrders()
        } catch(e) {
            ElMessage.error(e.response?.data?.message || '操作失败')
        }
    })
}

// Watch tab change
watch(activeTab, (val) => {
    if (val === 'order-mgmt') {
        fetchSellerOrders()
    }
})

onMounted(() => {
  fetchProfile()
  fetchApprenticeship()
  fetchResources()
  fetchProducts()
  fetchActivities()
  fetchSalesStats()
})
</script>

<style scoped>
.page { padding: 16px; }
.form-container { max-width: 600px; }
.toolbar { margin-bottom: 20px; }
.empty-text { text-align: center; color: #999; margin: 40px 0; }
.apprentice-card { margin-bottom: 20px; }
.student-header { display: flex; align-items: center; margin-bottom: 15px; }
.student-info { margin-left: 15px; }
.student-info .name { font-size: 16px; font-weight: bold; }
.student-info .id { font-size: 12px; color: #999; }
.apply-content { background: #f8f8f8; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
.content-text { font-size: 14px; line-height: 1.5; color: #333; }
.actions { display: flex; justify-content: flex-end; gap: 10px; }
.avatar-uploader .avatar { width: 100px; height: 100px; display: block; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; text-align: center; border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; display: flex; justify-content: center; align-items: center; }
.stat-num { font-size: 24px; font-weight: bold; color: #409EFF; text-align: center; padding: 10px 0; }

/* Ritual Animation */
.ritual-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}

.ritual-content {
  text-align: center;
  color: #fff;
  animation: fadeIn 1s ease-in;
}

.tea-cup {
  width: 150px;
  height: 150px;
  margin-bottom: 20px;
}

.steam {
  stroke-dasharray: 20;
  stroke-dashoffset: 20;
  animation: steamRise 2s infinite linear;
  opacity: 0;
}

.steam-1 { animation-delay: 0s; }
.steam-2 { animation-delay: 0.5s; }
.steam-3 { animation-delay: 1s; }

@keyframes steamRise {
  0% { stroke-dashoffset: 20; opacity: 0; transform: translateY(0); }
  50% { opacity: 1; }
  100% { stroke-dashoffset: 0; opacity: 0; transform: translateY(-20px); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.ritual-text h2 {
    font-size: 2.5em;
    margin-bottom: 10px;
    font-family: 'Kaiti', 'STKaiti', serif;
    color: #ffd700;
    text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}
.ritual-text p {
    font-size: 1.2em;
    color: #eee;
    letter-spacing: 2px;
}

/* Teaching Module Styles */
.official-comment {
    border: 2px solid #e6a23c;
    background-color: #fffdf0;
    padding: 10px;
    border-radius: 8px;
    margin-bottom: 10px;
    box-shadow: 0 2px 12px 0 rgba(230, 162, 60, 0.2);
}

.comment-item {
    padding: 10px;
    border-bottom: 1px solid #eee;
}

.comment-item.official-comment {
    border-bottom: 2px solid #e6a23c; /* Override for visual consistency if needed, but border is around the box */
}

.comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 5px;
}

.official-badge {
    margin-left: 8px;
    margin-right: 8px;
}

.comment-time {
    margin-left: auto;
    color: #999;
    font-size: 0.8em;
}

.comment-author {
    font-weight: bold;
    color: #333;
}
</style>
