<template>
  <div class="comment-container">
    <!-- 评论统计 -->
    <el-card class="stats-card" v-if="stats">
      <div class="rating-overview">
        <div class="rating-score">
          <h2>{{ averageRating }}</h2>
          <div class="rating-stars">
            <el-rate
              :value="Number(averageRating)"
              disabled
              show-score
              text-color="#ff9900"
            />
          </div>
          <div class="total-reviews">共 {{ stats.totalCount || 0 }} 条评价</div>
        </div>
        <div class="rating-bars">
          <div v-for="i in [5,4,3,2,1]" :key="i" class="rating-bar">
            <span class="star-label">{{ i }}星</span>
            <el-progress
              :percentage="getStarPercentage(i)"
              :stroke-width="12"
              :show-text="false"
              :color="progressColor"
            />
            <span class="star-count">{{ getStarCount(i) }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="comment-list">
      <div slot="header">
        <span>评论列表</span>
        <el-radio-group v-model="filterType" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="comment">评论</el-radio-button>
          <el-radio-button label="feedback">反馈</el-radio-button>
        </el-radio-group>
      </div>
      
      <el-timeline v-if="comments.length > 0">
        <el-timeline-item
          v-for="comment in filteredComments"
          :key="comment.id"
          :timestamp="formatTime(comment.createTime)"
          placement="top"
        >
          <el-card>
            <div class="comment-header">
              <el-avatar>{{ comment.userName ? comment.userName.charAt(0).toUpperCase() : 'U' }}</el-avatar>
              <span class="username">{{ comment.userName || '匿名用户' }}</span>
              <el-rate
                v-if="comment.type === 'comment'"
                :value="Number(comment.rating)"
                disabled
                show-score
                text-color="#ff9900"
              />
              <el-tag
                v-else
                type="warning"
              >反馈</el-tag>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-footer" v-if="comment.type === 'feedback'">
              <el-tag
                :type="getStatusType(comment.status)"
                size="small"
              >
                {{ getStatusText(comment.status) }}
              </el-tag>
              <div v-if="isAdmin" class="operation-buttons">
                <el-button 
                  type="success" 
                  size="mini" 
                  @click="handleStatus(comment.id, 'resolved')"
                  :disabled="comment.status === 'resolved'"
                >
                  标记已解决
                </el-button>
                <el-button 
                  type="danger" 
                  size="mini" 
                  @click="handleStatus(comment.id, 'rejected')"
                  :disabled="comment.status === 'rejected'"
                >
                  标记已拒绝
                </el-button>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <div v-else class="no-comments">
        暂无{{ filterType === 'feedback' ? '反馈' : filterType === 'comment' ? '评论' : '评论和反馈' }}
      </div>
    </el-card>

    <!-- 添加评论/反馈 -->
    <el-card class="comment-form">
      <div slot="header">
        <span>发表{{ newComment.type === 'feedback' ? '反馈' : '评论' }}</span>
        <el-radio-group v-model="newComment.type" size="small" style="margin-left: 20px;">
          <el-radio-button label="comment">评论</el-radio-button>
          <el-radio-button label="feedback">反馈</el-radio-button>
        </el-radio-group>
      </div>
      <el-form :model="newComment" :rules="rules" ref="commentForm">
        <el-form-item prop="rating" v-if="newComment.type === 'comment'">
          <el-rate
            v-model="newComment.rating"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          />
        </el-form-item>
        <el-form-item prop="content">
          <el-input
            type="textarea"
            v-model="newComment.content"
            :rows="4"
            :placeholder="newComment.type === 'feedback' ? '请输入您的反馈内容...' : '请输入您的评论...'"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitComment">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'CommentList',
  props: {
    goodsId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      user: null,
      comments: [],
      stats: {
        avgRating: 0,
        totalCount: 0,
        fiveStarCount: 0,
        fourStarCount: 0,
        threeStarCount: 0,
        twoStarCount: 0,
        oneStarCount: 0
      },
      filterType: 'all',
      newComment: {
        rating: 0,
        content: '',
        type: 'comment'
      },
      rules: {
        rating: [
          { required: true, message: '请选择评分', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' },
          { min: 5, message: '内容至少 5 个字符', trigger: 'blur' }
        ]
      },
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      progressColor: '#ff9900'
    }
  },
  computed: {
    isAdmin() {
      return this.user && (this.user.roleId === 0 || this.user.roleId === 1);
    },
    averageRating() {
      return this.stats && typeof this.stats.avgRating !== 'undefined' 
        ? Number(this.stats.avgRating).toFixed(1) 
        : '0.0';
    },
    filteredComments() {
      if (this.filterType === 'all') {
        return this.comments;
      }
      return this.comments.filter(c => c.type === this.filterType);
    }
  },
  methods: {
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },

    async loadComments() {
      try {
        console.log('Loading comments for goodsId:', this.goodsId);
        const res = await this.$axios.get(this.$httpUrl + `/comment/list/${this.goodsId}`);
        console.log('Comments API response:', res);
        
        if (res.data.code === 200) {
          this.comments = Array.isArray(res.data.data) ? res.data.data : [];
          this.comments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
        } else {
          console.error('加载评论失败:', res.data);
          this.$message.error(res.data.msg || '加载评论失败');
        }
      } catch (error) {
        console.error('加载评论失败:', error);
        this.$message.error('加载评论失败: ' + (error.response?.data?.msg || error.message));
      }
    },

    async loadStats() {
      try {
        console.log('Loading stats for goodsId:', this.goodsId);
        const res = await this.$axios.get(this.$httpUrl + `/comment/stats/${this.goodsId}`);
        console.log('Stats response:', res);
        
        if (res.data.code === 200) {
          // 确保所有计数字段都有值
          this.stats = {
            avgRating: Number(res.data.data.avgRating || 0),
            totalCount: Number(res.data.data.totalCount || 0),
            fiveStarCount: Number(res.data.data.fiveStarCount || 0),
            fourStarCount: Number(res.data.data.fourStarCount || 0),
            threeStarCount: Number(res.data.data.threeStarCount || 0),
            twoStarCount: Number(res.data.data.twoStarCount || 0),
            oneStarCount: Number(res.data.data.oneStarCount || 0)
          };
          console.log('Processed stats:', this.stats);
        } else {
          console.error('加载统计信息失败:', res.data);
          this.$message.error(res.data.msg || '加载统计信息失败');
        }
      } catch (error) {
        console.error('加载统计信息失败:', error);
        this.$message.error('加载统计信息失败: ' + (error.response?.data?.msg || error.message));
      }
    },

    getStarCount(star) {
      if (!this.stats) return 0;
      const countKey = `${this.getStarNumberText(star)}StarCount`;
      return this.stats[countKey] || 0;
    },

    getStarPercentage(star) {
      if (!this.stats || this.stats.totalCount === 0) return 0;
      const count = this.getStarCount(star);
      return Math.round((count / this.stats.totalCount) * 100);
    },

    getStarNumberText(num) {
      const numbers = ['zero', 'one', 'two', 'three', 'four', 'five'];
      return numbers[num];
    },

    async refreshComments() {
      await Promise.all([
        this.loadComments(),
        this.loadStats()
      ]);
    },

    async submitComment() {
      if (!this.user) {
        this.$message.error('请先登录');
        return;
      }

      // 如果是评论类型，验证评分
      if (this.newComment.type === 'comment') {
        if (!this.newComment.rating) {
          this.$message.error('请选择评分');
          return;
        }
      }

      try {
        await this.$refs.commentForm.validate();
        
        const comment = {
          ...this.newComment,
          goodsId: this.goodsId,
          userId: this.user.id,
          status: 'pending' // 新提交的反馈默认为待处理状态
        };

        console.log('Submitting:', comment);
        const res = await this.$axios.post(this.$httpUrl + '/comment/add', comment);
        
        if (res.data.code === 200) {
          this.$message.success(comment.type === 'feedback' ? '反馈提交成功' : '评论提交成功');
          this.$refs.commentForm.resetFields();
          this.newComment.rating = 0;
          this.newComment.content = '';
          await this.loadComments();
        } else {
          this.$message.error(res.data.msg || '提交失败');
        }
      } catch (error) {
        console.error('提交失败:', error);
        this.$message.error('提交失败: ' + (error.response?.data?.msg || error.message));
      }
    },

    async handleStatus(commentId, status) {
      try {
        console.log('Updating status:', { commentId, status });
        
        // 使用 POST 方法代替 PUT，因为某些情况下 PUT 可能有问题
        const res = await this.$axios.post(
          this.$httpUrl + `/comment/status/${commentId}`,
          null,
          { 
            params: { status },
            headers: {
              'Content-Type': 'application/json'
            }
          }
        );
        
        console.log('Update status response:', res);

        if (res.data.code === 200) {
          this.$message.success('状态更新成功');
          // 重新加载评论列表
          await this.loadComments();
        } else {
          console.error('状态更新失败:', res.data);
          this.$message.error(res.data.msg || '状态更新失败');
        }
      } catch (error) {
        console.error('状态更新失败:', error);
        this.$message.error('状态更新失败: ' + (error.response?.data?.msg || error.message));
      }
    },

    getStatusType(status) {
      const types = {
        pending: 'warning',
        resolved: 'success',
        rejected: 'danger'
      };
      return types[status] || 'info';
    },

    getStatusText(status) {
      const texts = {
        pending: '待处理',
        resolved: '已解决',
        rejected: '已拒绝'
      };
      return texts[status] || '未知状态';
    },

    initUser() {
      const userStr = sessionStorage.getItem('CurUser');
      if (userStr) {
        try {
          this.user = JSON.parse(userStr);
          console.log('Current user:', this.user);
        } catch (error) {
          console.error('解析用户信息失败:', error);
          this.$message.error('获取用户信息失败');
        }
      }
    }
  },
  created() {
    this.initUser();
    if (this.goodsId) {
      console.log('Initializing comments for goodsId:', this.goodsId);
      this.refreshComments();
    } else {
      console.error('No goodsId provided');
      this.$message.error('缺少商品ID');
    }
  }
};
</script>

<style scoped>
.comment-container {
  padding: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.rating-overview {
  display: flex;
  align-items: flex-start;
  gap: 40px;
}

.rating-score {
  text-align: center;
  min-width: 200px;
}

.rating-score h2 {
  font-size: 36px;
  margin: 0;
  color: #ff9900;
}

.total-reviews {
  color: #909399;
  margin-top: 10px;
}

.rating-bars {
  flex-grow: 1;
  padding: 0 20px;
}

.rating-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.star-label {
  width: 40px;
  text-align: right;
  margin-right: 10px;
  color: #606266;
}

.star-count {
  width: 40px;
  text-align: left;
  margin-left: 10px;
  color: #606266;
}

.rating-bar .el-progress {
  flex-grow: 1;
}

/* 确保进度条样式正确 */
:deep(.el-progress-bar__outer) {
  background-color: #ebeef5;
}

:deep(.el-progress-bar__inner) {
  transition: all 0.3s;
}

.comment-list {
  margin-bottom: 20px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.username {
  font-weight: bold;
}

.comment-content {
  margin: 10px 0;
  color: #303133;
  white-space: pre-wrap;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.operation-buttons {
  display: flex;
  gap: 10px;
}

.el-radio-group {
  margin-left: 20px;
}

.no-comments {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>





