<template>
  <div>
    <h3>导入历史记录</h3>
    
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="type" label="导入类型" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 'goods' ? 'primary' : 'success'">
            {{ scope.row.type === 'goods' ? '数据资产' : '入库记录' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileName" label="文件名" width="200"></el-table-column>
      <el-table-column prop="totalCount" label="总数" width="80"></el-table-column>
      <el-table-column prop="successCount" label="成功" width="80"></el-table-column>
      <el-table-column prop="failCount" label="失败" width="80"></el-table-column>
<!--      <el-table-column prop="userName" label="操作人" width="120"></el-table-column>-->
      <el-table-column prop="createTime" label="导入时间" width="160">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button 
            type="text" 
            @click="viewDetails(scope.row)"
            :disabled="scope.row.failCount === 0">
            查看错误详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pageNum"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>
    
    <!-- 错误详情对话框 -->
    <el-dialog title="导入错误详情" :visible.sync="detailsVisible" width="600px">
      <div v-if="selectedRecord">
        <p>文件名: {{ selectedRecord.fileName }}</p>
        <p>导入时间: {{ formatTime(selectedRecord.createTime) }}</p>
        <p>导入结果: 总数 {{ selectedRecord.totalCount }}, 成功 {{ selectedRecord.successCount }}, 失败 {{ selectedRecord.failCount }}</p>
        
        <h4>错误明细:</h4>
        <ul class="error-list">
          <li v-for="(error, index) in errorDetails" :key="index">{{ error }}</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "ImportHistoryList",
  data() {
    return {
      tableData: [],
      pageSize: 10,
      pageNum: 1,
      total: 0,
      detailsVisible: false,
      selectedRecord: null,
      errorDetails: []
    }
  },
  methods: {
    formatTime(timestamp) {
      if (!timestamp) return '-';
      return new Date(timestamp).toLocaleString();
    },
    
    loadData() {
      this.$axios.post(this.$httpUrl + '/import/history/list', {
        pageSize: this.pageSize,
        pageNum: this.pageNum
      }).then(res => res.data).then(res => {
        if (res.code == 200) {
          this.tableData = res.data;
          this.total = res.total;
        } else {
          this.$message.error('获取导入历史记录失败');
        }
      }).catch(err => {
        console.error('加载导入历史失败:', err);
        this.$message.error('加载导入历史失败');
      });
    },
    
    handleSizeChange(val) {
      this.pageNum = 1;
      this.pageSize = val;
      this.loadData();
    },
    
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadData();
    },
    
    viewDetails(row) {
      this.selectedRecord = row;
      
      try {
        this.errorDetails = JSON.parse(row.errorDetails || '[]');
      } catch (e) {
        this.errorDetails = [row.errorDetails || '无错误详情'];
      }
      
      this.detailsVisible = true;
    }
  },
  created() {
    this.loadData();
  }
}
</script>

<style scoped>
.error-list {
  color: #F56C6C;
  margin-top: 10px;
  padding-left: 20px;
}
</style>
