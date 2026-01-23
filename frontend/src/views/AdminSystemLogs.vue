<template>
  <div class="page">
    <el-card>
      <h2>系统日志</h2>
      <el-timeline style="margin-top: 20px">
        <el-timeline-item timestamp="2024-05-24 10:23:00" placement="top">
          <el-card>
            <h4>用户登录</h4>
            <p>User: admin (ID: 1) logged in successfully.</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item timestamp="2024-05-24 09:12:00" placement="top">
          <el-card>
            <h4>资源上传</h4>
            <p>User: inheritor01 uploaded new resource "柳琴戏教学01.mp4".</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item timestamp="2024-05-23 20:45:00" placement="top">
          <el-card>
            <h4>系统警告</h4>
            <p>Database connection pool usage > 80%.</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
</style>
