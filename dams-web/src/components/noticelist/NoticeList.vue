<template>
  <div class="notice-container">
    <!-- 添加通知按钮 -->
    <div class="operation-bar">
      <el-button type="primary" @click="showAddDialog">发布通知</el-button>
    </div>

    <!-- 通知列表 -->
    <el-table :data="notices" style="width: 100%">
      <el-table-column prop="title" label="标题" width="180"></el-table-column>
      <el-table-column prop="content" label="内容"></el-table-column>
      <el-table-column prop="time" label="发布时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.time) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑通知对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="50%">
      <el-form :model="noticeForm" :rules="rules" ref="noticeForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" v-model="noticeForm.content" :rows="4"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'NoticeList',
  data() {
    return {
      notices: [],
      dialogVisible: false,
      dialogTitle: '发布通知',
      noticeForm: {
        id: null,
        title: '',
        content: '',
        time: null
      },
      rules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchNotices()
  },
  methods: {
    // 获取通知列表
    async fetchNotices() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/notice/list')
        console.log('获取通知列表响应:', res)
        if (res.data.code === 200) {
          this.notices = res.data.data || []
        } else {
          this.$message.error(res.data.msg || '获取通知列表失败')
        }
      } catch (error) {
        console.error('获取通知列表错误:', error)
        this.$message.error('获取通知列表失败: ' + (error.response?.data?.msg || error.message))
      }
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString()
    },

    // 提交表单
    submitForm() {
      this.$refs.noticeForm.validate(async (valid) => {
        if (valid) {
          try {
            const url = this.noticeForm.id ? '/notice/update' : '/notice/save'
            const submitData = {
              id: this.noticeForm.id,
              title: this.noticeForm.title,
              content: this.noticeForm.content
            }
            
            console.log('提交的通知数据:', submitData)
            const res = await this.$axios.post(this.$httpUrl + url, submitData)
            console.log('提交通知响应:', res)
            
            if (res.data.code === 200) {
              this.$message.success(this.noticeForm.id ? '更新成功' : '发布成功')
              this.dialogVisible = false
              this.fetchNotices()
            } else {
              this.$message.error(res.data.msg || (this.noticeForm.id ? '更新失败' : '发布失败'))
            }
          } catch (error) {
            console.error('提交通知错误:', error)
            this.$message.error((this.noticeForm.id ? '更新' : '发布') + '失败: ' + 
              (error.response?.data?.msg || error.message))
          }
        }
      })
    },

    // 重置表单
    resetForm() {
      this.noticeForm = {
        id: null,
        title: '',
        content: '',
        time: null
      }
    },

    // 显示添加对话框
    showAddDialog() {
      this.dialogTitle = '发布通知'
      this.resetForm()
      this.dialogVisible = true
    },

    // 显示编辑对话框
    handleEdit(row) {
      this.dialogTitle = '编辑通知'
      this.noticeForm = {
        id: row.id,
        title: row.title,
        content: row.content
      }
      this.dialogVisible = true
    },

    // 删除通知
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该通知?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const res = await this.$axios.delete(this.$httpUrl + `/notice/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchNotices()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.notice-container {
  padding: 20px;
}
.operation-bar {
  margin-bottom: 20px;
}
</style>








