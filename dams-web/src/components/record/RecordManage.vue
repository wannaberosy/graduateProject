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


    </div>
    <el-table :data="tableData"
              :header-cell-style="{ background: '#f2f5fc', color: '#555555' }"
              border
    >
      <el-table-column prop="id" label="ID" width="60">
      </el-table-column>
      <el-table-column label="数据资产名" width="180">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.goodsname" placement="top" effect="light">
            <span>{{ scope.row.goodsname || scope.row.goods || '-' }}</span>
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
<!--      <el-table-column label="操作人" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.adminname || '-' }}</span>
        </template>
      </el-table-column>-->
      <el-table-column label="申请人" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.username || scope.row.userid || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="数量" width="180">
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

  </div>
</template>

<script>
export default {
  name: "RecordManage",
  data() {

    return {
      user : JSON.parse(sessionStorage.getItem('CurUser')),
      storage:'',
      goodstype:'',
      goodstypeData:[],
      storageData:[],
      tableData: [],
      pageSize:10,
      pageNum:1,
      total:0,
      name:'',
      centerDialogVisible:false,
      form:{
        id:'',
        name:'',
        storage:'',
        goodstype:'',
        count:'',
        remark:''
      },
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
        }else{
          this.$message.error('获取数据失败')
        }

      }).catch(err => {
        console.error('加载记录失败:', err)
        this.$message.error('加载记录失败')
      })
    },
    loadStorage(){
      this.$axios.get(this.$httpUrl+'/storage/list').then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){
          this.storageData=res.data
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
        }else{
          this.$message.error('获取分类数据失败')
        }

      })
    }
  },

  beforeMount() {
    this.loadStorage()
    this.loadGoodstype()
    this.loadPost()
  }
}
</script>

<style scoped>
.el-table .cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>


