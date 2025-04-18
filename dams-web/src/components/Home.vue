<template>
  <div style="text-align: center;background-color: #f1f1f3;height: 100%;padding: 0px;margin: 0px;">
    <h1 style="font-size: 50px;">{{'欢迎你！'+user.name}}</h1>
    <el-descriptions  title="个人中心" :column="2" size="40" border>
      <el-descriptions-item>
        <template slot="label">
          <i class="el-icon-s-custom"></i>
          账号
        </template>
        {{user.no}}
      </el-descriptions-item>
      <el-descriptions-item>
        <template slot="label">
          <i class="el-icon-mobile-phone"></i>
          电话
        </template>
        {{user.phone}}
      </el-descriptions-item>
      <el-descriptions-item>
        <template slot="label">
          <i class="el-icon-location-outline"></i>
          性别
        </template>
        <el-tag
            :type="user.sex === '1' ? 'primary' : 'danger'"
            disable-transitions><i :class="user.sex==1?'el-icon-male':'el-icon-female'"></i>{{user.sex==1?"男":"女"}}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item>
        <template slot="label">
          <i class="el-icon-tickets"></i>
          角色
        </template>
        <el-tag
            type="success"
            disable-transitions>{{user.roleId==0?"超级管理员":(user.roleId==1?"管理员":"用户")}}</el-tag>

      </el-descriptions-item>
    </el-descriptions>

    <!-- 通知公告部分 -->
    <div class="notice-section">
      <el-card class="notice-card">
        <div slot="header" class="notice-header">
          <span><i class="el-icon-bell"></i> 通知公告</span>
          <el-button 
            v-if="user.roleId <= 1" 
            style="float: right; padding: 3px 0" 
            type="text"
            @click="goToNoticeManage">
            管理通知
          </el-button>
        </div>
        <div v-if="notices.length > 0" class="notice-list">
          <el-collapse v-model="activeNotices">
            <el-collapse-item 
              v-for="notice in notices" 
              :key="notice.id"
              :title="notice.title"
              :name="notice.id">
              <div class="notice-info">
                <span class="notice-time">发布时间：{{ formatDate(notice.time) }}</span>
              </div>
              <div class="notice-content">{{ notice.content }}</div>
            </el-collapse-item>
          </el-collapse>
        </div>
        <div v-else class="no-notice">
          暂无通知公告
        </div>
      </el-card>
    </div>

    <DateUtils></DateUtils>
  </div>
</template>

<script>
import DateUtils from "./DateUtils";
export default {
  name: "Home",
  components: {DateUtils},
  data() {
    return {
      user: {},
      notices: [],
      activeNotices: [], // 当前展开的通知
    }
  },
  computed:{

  },
  methods:{
    init(){
      this.user = JSON.parse(sessionStorage.getItem('CurUser'))
      this.fetchNotices()
    },
    
    // 获取通知列表
    async fetchNotices() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/notice/list')
        if (res.data.code === 200) {
          this.notices = res.data.data || []
          // 按时间倒序排序，最新的在前面
          this.notices.sort((a, b) => new Date(b.time) - new Date(a.time))
        } else {
          console.error('获取通知列表失败:', res.data.msg)
        }
      } catch (error) {
        console.error('获取通知列表错误:', error)
      }
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },

    // 跳转到通知管理页面
    goToNoticeManage() {
      this.$router.push('/notice')
    }
  },
  created(){
    this.init()
  }
}
</script>

<style scoped>
.el-descriptions{
  width:90%;

  margin: 0 auto;
  text-align: center;
}

.notice-section {
  width: 90%;
  margin: 20px auto;
}

.notice-card {
  text-align: left;
  margin-bottom: 20px;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-header i {
  margin-right: 5px;
  color: #409EFF;
}

.notice-list {
  max-height: 400px;
  overflow-y: auto;
}

.notice-info {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.notice-content {
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

.no-notice {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

/* 自定义折叠面板样式 */
.el-collapse {
  border: none;
}

.el-collapse-item__header {
  font-size: 16px;
  color: #303133;
  background-color: #f5f7fa;
  padding: 0 15px;
}

.el-collapse-item__content {
  padding: 15px;
  background-color: #fff;
}

/* 滚动条样式 */
.notice-list::-webkit-scrollbar {
  width: 6px;
}

.notice-list::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.notice-list::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>