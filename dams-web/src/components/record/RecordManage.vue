<template>
  <div>
    <div style="margin-bottom: 5px;">
      <el-input v-model="name" placeholder="请输入数据资产名" suffix-icon="el-icon-search" style="width: 200px;"
                @keyup.enter.native="loadPost"></el-input>
      <el-select v-model="storage" clearable placeholder="请选择仓库" style="margin-left: 5px">
        <el-option
            v-for="item in storageData"
            :key="item.id"
            :label="item.name"
            :value="item.id">
        </el-option>
      </el-select>

      <el-select v-model="goodstype" clearable placeholder="请选择分类" style="margin-left: 5px">
        <el-option
            v-for="item in goodstypeData"
            :key="item.id"
            :label="item.name"
            :value="item.id">
        </el-option>
      </el-select>
      <el-button type="primary" style="margin-left: 5px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam">重置</el-button>
<!--      <el-button type="warning" @click="showImportDialog" style="float: right; margin-left: 10px;">批量入库</el-button>-->
    </div>
    <el-table :data="mergedTableData"
              :header-cell-style="{ background: '#f2f5fc', color: '#555555' }"
              border
    >
      <el-table-column prop="id" label="ID" width="60">
      </el-table-column>
      <el-table-column label="数据资产名" width="180">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.goodsname" placement="top" effect="light">
            <span>{{ scope.row.goodsname || '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="仓库" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.storagename || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.typename || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.username || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="180">
        <template slot-scope="scope">
          <span :class="{ 'in-count': isInRecord(scope.row), 'out-count': isOutRecord(scope.row) }">
            {{ formatCount(scope.row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="isInRecord(scope.row) ? 'success' : 'danger'">
            {{ isInRecord(scope.row) ? '入库' : '出库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createtime" label="操作时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createtime) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.remark" placement="top" effect="light">
            <span>{{ scope.row.remark || '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>

    </el-table>
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20,30]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
    </el-pagination>

    <import-data-dialog 
      :visible.sync="importDialogVisible" 
      :import-type="'records'" 
      :show-type-select="false" 
      title="批量入库记录导入"
      @import-success="loadPost"
    />
  </div>
</template>

<script>
import ImportDataDialog from '../common/ImportDataDialog.vue';

export default {
  name: "RecordManage",
  components: {
    ImportDataDialog
  },
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem('CurUser')),
      storage: '',
      goodstype: '',
      goodstypeData: [],
      storageData: [],
      goodsList: [],
      userList: [],
      tableData: [],
      pageSize: 10,
      pageNum: 1,
      total: 0,
      name: '',
      centerDialogVisible: false,
      form: {
        id: '',
        name: '',
        storage: '',
        goodstype: '',
        count: '',
        remark: ''
      },
      dataLoaded: {
        goods: false,
        users: false,
        storage: false,
        goodstype: false
      },
      importDialogVisible: false
    }
  },
  computed: {
    mergedTableData() {
      if (!this.tableData || this.tableData.length === 0) {
        return [];
      }
      
      return this.tableData.map(record => {
        const result = { ...record };
        
        if (record.goods && this.goodsList && this.goodsList.length > 0) {
          const goods = this.goodsList.find(g => g.id == record.goods);
          result.goodsname = goods ? goods.name : '-';
        } else {
          result.goodsname = '-';
        }
        
        console.log('处理用户名称:', record);
        
        const possibleUserIdFields = ['userId', 'userid', 'user_id', 'user'];
        let userId = null;
        for (const field of possibleUserIdFields) {
          if (record[field] !== undefined && record[field] !== null) {
            userId = record[field];
            console.log('找到用户ID字段:', field, userId);
            break;
          }
        }
        
        if (userId && this.userList && this.userList.length > 0) {
          console.log('尝试匹配用户:', userId, this.userList);
          const user = this.userList.find(u => {
            console.log('比较:', u.id, userId, u.id == userId);
            return u.id == userId;
          });
          result.username = user ? user.name : '-';
          console.log('匹配结果:', user, result.username);
        } else {
          result.username = '-';
        }
        
        if (record.storage && this.storageData && this.storageData.length > 0) {
          const storage = this.storageData.find(s => s.id == record.storage);
          result.storagename = storage ? storage.name : '-';
        } else {
          result.storagename = record.storagename || '-';
        }
        
        if (record.goodstype && this.goodstypeData && this.goodstypeData.length > 0) {
          const type = this.goodstypeData.find(t => t.id == record.goodstype);
          result.typename = type ? type.name : '-';
        } else {
          result.typename = record.typename || '-';
        }
        
        return result;
      });
    }
  },
  methods:{
    formatTime(timestamp) {
      if (!timestamp) return '-';
      return new Date(timestamp).toLocaleString();
    },
    resetForm() {
      this.$refs.form.resetFields();
    },

    handleSizeChange(val) {
      console.log(`每页 ${val} 条`);
      this.pageNum=1
      this.pageSize=val
      this.loadPost()
    },
    handleCurrentChange(val) {
      console.log(`当前页: ${val}`);
      this.pageNum=val
      this.loadPost()
    },
    resetParam(){
      this.name=''
      this.storage=''
      this.goodstype=''
      this.loadPost()
    },
    loadPost(){
      this.$axios.post(this.$httpUrl+'/record/listPage',{
        pageSize:this.pageSize,
        pageNum:this.pageNum,
        param:{
          name:this.name,
          goodstype:this.goodstype+'',
          storage:this.storage+'',
          roleId:this.user.roleId+'',
          userId:this.user.id+''
        }
      }).then(res=>res.data).then(res=>{
        console.log('Record data:', res.data);
        if(res.code==200){
          this.tableData=res.data
          this.total=res.total
          this.ensureReferenceDataLoaded()
        }else{
          this.$message.error('获取数据失败')
        }

      }).catch(err => {
        console.error('加载记录失败:', err)
        this.$message.error('加载记录失败')
      })
    },
    ensureReferenceDataLoaded() {
      if (!this.dataLoaded.goods) {
        this.loadGoods();
      }
      if (!this.dataLoaded.users) {
        this.loadUsers();
      }
      if (!this.dataLoaded.storage) {
        this.loadStorage();
      }
      if (!this.dataLoaded.goodstype) {
        this.loadGoodstype();
      }
    },
    loadStorage(){
      this.$axios.get(this.$httpUrl+'/storage/list').then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){
          this.storageData=res.data
          this.dataLoaded.storage = true;
        }else{
          this.$message.error('获取仓库数据失败')
        }

      })
    },
    loadGoodstype(){
      this.$axios.get(this.$httpUrl+'/goodstype/list').then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){
          this.goodstypeData=res.data
          this.dataLoaded.goodstype = true;
        }else{
          this.$message.error('获取分类数据失败')
        }

      })
    },
    loadGoods(){
      this.$axios.get(this.$httpUrl+'/goods/list').then(res=>res.data).then(res=>{
        if(res.code==200){
          this.goodsList=res.data
          this.dataLoaded.goods = true;
        }else{
          this.$message.error('获取商品数据失败')
        }
      }).catch(err => {
        console.error('加载商品失败:', err)
        this.$message.error('加载商品失败')
      })
    },
    loadUsers(){
      this.$axios.get(this.$httpUrl+'/user/list').then(res=>res.data).then(res=>{
        if(res.code==200){
          this.userList=res.data
          this.dataLoaded.users = true;
        }else{
          this.$message.error('获取用户数据失败')
        }
      }).catch(err => {
        console.error('加载用户失败:', err)
        this.$message.error('加载用户失败')
      })
    },
    isInRecord(record) {
      return record.action === '1' || (record.action === 1);
    },
    isOutRecord(record) {
      return record.action === '2' || (record.action === 2);
    },
    formatCount(record) {
      if (!record || record.count === undefined) return '-';
      
      if (this.isInRecord(record)) {
        return '+' + record.count;
      } else if (this.isOutRecord(record)) {
        return '-' + record.count;
      } else {
        return record.count;
      }
    },
    showImportDialog() {
      this.importDialogVisible = true;
    }
  },

  beforeMount() {
    Promise.all([
      this.loadStorage(),
      this.loadGoodstype(),
      this.loadGoods(),
      this.loadUsers()
    ]).then(() => {
      console.log('所有引用数据加载完成，开始加载主数据');
      this.loadPost();
    }).catch(error => {
      console.error('引用数据加载失败:', error);
      this.loadPost();
    });
  }
}
</script>

<style scoped>
.el-table .cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.in-count {
  color: #67C23A;
  font-weight: bold;
}

.out-count {
  color: #F56C6C;
  font-weight: bold;
}
</style>


