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

      <el-button type="primary" style="margin-left: 10px;" @click="add" v-if="user.roleId!=2">新增</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="inGoods" v-if="user.roleId!=2">入库</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="outGoods" v-if="user.roleId!=2">出库</el-button>
      <el-button type="warning" @click="showImportDialog" v-if="user.roleId!=2">批量导入</el-button>

    </div>
    <el-table :data="tableData"
              :header-cell-style="{ background: '#f2f5fc', color: '#555555' }"
              border
              highlight-current-row
              @current-change="selectCurrentChange"
    >
      <el-table-column prop="id" label="ID" width="60">
      </el-table-column>
      <el-table-column prop="name" label="数据资产名" width="180" >
      </el-table-column>
      <el-table-column prop="storage" label="仓库" width="180" :formatter="formatStorage">
      </el-table-column>
      <el-table-column prop="goodstype" label="分类" width="180" :formatter="formatGoodstype">
      </el-table-column>
      <el-table-column prop="count" label="数量" width="180">
      </el-table-column>
      <el-table-column prop="remark" label="备注">
      </el-table-column>
      <el-table-column prop="operate" label="操作">
        <template slot-scope="scope">
          <el-button size="small" type="success" @click="mod(scope.row)" v-if="user.roleId!=2">编辑</el-button>
          <el-popconfirm
              title="确定删除吗？"
              @confirm="del(scope.row.id)"
              style="margin-left: 5px;"
          >
            <el-button slot="reference" size="small" type="danger" style="margin-left: 5px" v-if="user.roleId!=2" >删除</el-button>
          </el-popconfirm>
          <el-button 
            size="small" 
            type="primary" 
            @click="showComments(scope.row)"
            style="margin-left: 5px"
          >
            评论与反馈
          </el-button>
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

    <el-dialog
        title="数据资产编辑"
        :visible.sync="centerDialogVisible"
        width="30%"
        center>

      <el-form ref="form" :rules="rules" :model="form" label-width="100px">
        <el-form-item label="数据资产名" prop="name">
          <el-col :span="20">
            <el-input v-model="form.name"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="仓库" prop="storage">



          <el-col :span="20">
            <el-select v-model="form.storage" clearable placeholder="请选择仓库" style="margin-left: 5px">
              <el-option
                  v-for="item in storageData"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item label="分类" prop="goodstype">

          <el-col :span="20">
            <el-select v-model="form.goodstype" clearable placeholder="请选择分类" style="margin-left: 5px">
              <el-option
                  v-for="item in goodstypeData"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item label="数量" prop="count">
          <el-col :span="20">
            <el-input v-model="form.count"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-col :span="20">
            <el-input type="textarea" v-model="form.remark"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
    <el-button @click="centerDialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="save">确 定</el-button>
  </span>
    </el-dialog>

    <el-dialog
        title="出入库"
        :visible.sync="inDialogVisible"
        width="30%"
        center>
      <el-dialog
          width="50%"
          title="用户选择"
          :visible.sync="innerVisible"
          append-to-body>
        <SelectUser @doSelectUser="doSelectUser"></SelectUser>
        <span slot="footer" class="dialog-footer">
          <el-button @click="innerVisible = false">取 消</el-button>
          <el-button type="primary" @click="confirmUser">确 定</el-button>
        </span>
      </el-dialog>

      <el-form ref="formIn" :rules="rulesIn" :model="formIn" label-width="100px">
        <el-form-item label="数据资产名" prop="name">
          <el-col :span="20">
            <el-input v-model="formIn.goodsname" readonly></el-input>
          </el-col>
        </el-form-item>

        <el-form-item label="申请人" prop="name">
          <el-col :span="20">
            <el-input v-model="formIn.username" @click.native="selectUser"></el-input>
          </el-col>
        </el-form-item>

        <el-form-item label="数量" prop="count">
          <el-col :span="20">
            <el-input v-model="formIn.count"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-col :span="20">
            <el-input type="textarea" v-model="formIn.remark"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
    <el-button @click="inDialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="doInGoodsSave">确 定</el-button>
  </span>
    </el-dialog>

    <el-dialog
        title="资产评论与反馈"
        :visible.sync="commentDialogVisible"
        width="60%"
        center>
      <comment-list 
        v-if="commentDialogVisible"
        :goods-id="currentRow.id"
      />
    </el-dialog>

    <import-data-dialog 
      :visible.sync="importDialogVisible" 
      :import-type="'goods'" 
      :show-type-select="false" 
      title="批量导入数据资产"
      @import-success="loadPost"
    />
  </div>
</template>

<script>
import SelectUser from "../user/SelectUser.vue";
import CommentList from '../commentlist/CommentList.vue'
import ImportDataDialog from '../common/ImportDataDialog.vue'

export default {
  name: "GoodsManage",
  components:{SelectUser, CommentList, ImportDataDialog},
  data() {
    let checkDuplicate =(rule,value,callback)=>{
      if(this.form.id){
        return callback();
      }
      this.$axios.get(this.$httpUrl+"/goods/findByName?name="+this.form.name).then(res=>res.data).then(res=>{
        if(res.code!=200){
          callback()
        }else{
          callback(new Error('仓库名已经存在'));
        }
      })
    };
    let checkCount=(rule,value,callback)=>{
      if(value>9999){
        callback(new Error('数量输入过大'));
      }else{
        callback();
      }
    };
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
      inDialogVisible:false,
      innerVisible:false,
      currentRow:{},
      tempUser:{},
      form:{
        id:'',
        name:'',
        storage:'',
        goodstype:'',
        count:'',
        remark:''
      },
      formIn:{
        goods: '',
        goodsname: '',
        userid: '',
        username: '',
        count: '',
        remark: '',
        action: '1'  // 默认为入库
      },
      rulesIn:{

      },
      rules: {
        name: [
          {required: true, message: '请输入数据资产名', trigger: 'blur'},
          {validator:checkDuplicate,trigger: 'blur'}
        ],
        storage:[
          {required: true, message: '请选择仓库', trigger: 'blur'}
        ],
        goodstype:[
          {required: true, message: '请选择分类', trigger: 'blur'}
        ],
        count:[
          {required:true,message:'请输入数量',trigger:'blur'},
          {pattern:/^([1-9][0-9]*){1,4}$/,message: '数量必须为正整数',trigger: 'blur'},
          {validator:checkCount,trigger: 'blur'}

        ]
      },
      commentDialogVisible: false,
      importDialogVisible: false
    }
  },
  methods:{
    doSelectUser(val){
      console.log(val)
      this.tempUser=val
    },
    confirmUser(){
      this.formIn.username=this.tempUser.name
      this.formIn.userid=this.tempUser.id
      this.innerVisible=false
    },
    selectCurrentChange(val){
      this.currentRow=val;
    },
    formatStorage(row){
      let temp = this.storageData.find(item=>{
        return item.id == row.storage
      })
      return temp&&temp.name
    },

    formatGoodstype(row){
      let temp = this.goodstypeData.find(item=>{
        return item.id == row.goodstype
      })
      return temp&&temp.name
    },
    resetForm() {
      this.$refs.form.resetFields();
    },
    resetInForm(){
      this.$refs.formIn.resetFields();
    },
    del(id){
      console.log(id)

      this.$axios.get(this.$httpUrl+'/goods/del?id='+id).then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){

          this.$message({
            message: '操作成功！',
            type: 'success'
          });
          this.loadPost()
        }else{
          this.$message({
            message: '操作失败！',
            type: 'error'
          });
        }

      })
    },
    mod(row){
      this.centerDialogVisible = true
      this.$nextTick(()=>{
        //赋值到表单
        this.form.id = row.id
        this.form.name = row.name
        this.form.remark = row.remark
        this.form.count=row.count
        this.form.goodstype=row.goodstype
        this.form.storage=row.storage
      })
    },
    inGoods(){
      if(!this.currentRow.id) {
        this.$message.warning('请选择记录');
        return;
      }
      this.inDialogVisible = true;
      this.formIn.username = '';
      this.$nextTick(() => {
        this.resetInForm();
      });

      this.formIn.goodsname = this.currentRow.name;
      this.formIn.goods = this.currentRow.id;
      this.formIn.action = '1';  // 设置为入库
    },
    outGoods(){
      if(!this.currentRow.id) {
        this.$message.warning('请选择记录');
        return;
      }
      this.inDialogVisible = true;
      this.formIn.username = '';
      this.$nextTick(() => {
        this.resetInForm();
      });

      this.formIn.goodsname = this.currentRow.name;
      this.formIn.goods = this.currentRow.id;
      this.formIn.action = '2';  // 设置为出库
    },
    selectUser(){
      this.innerVisible=true
    },
    add(){
      this.form.id=''
      this.centerDialogVisible = true
      this.$nextTick(()=>{
        this.resetForm()
      })

    },
    doSave(){
      this.$axios.post(this.$httpUrl+'/goods/save',this.form).then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){

          this.$message({
            message: '操作成功！',
            type: 'success'
          });
          this.centerDialogVisible = false
          this.loadPost()
          this. resetForm()
        }else{
          this.$message({
            message: '操作失败！',
            type: 'error'
          });
        }

      })
    },
    doInGoodsSave(){
      this.$refs.formIn.validate((valid) => {
        if (valid) {
          this.$axios.post(this.$httpUrl + '/record/save', this.formIn)
            .then(res => res.data)
            .then(res => {
              if (res.code == 200) {
                this.$message({
                  message: '操作成功！',
                  type: 'success'
                });
                this.inDialogVisible = false;
                this.loadPost();
                this.resetInForm();
              } else {
                this.$message({
                  message: '操作失败！',
                  type: 'error'
                });
              }
            });
        }
      });
    },
    doMod(){
      this.$axios.post(this.$httpUrl+'/goods/update',this.form).then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){

          this.$message({
            message: '操作成功！',
            type: 'success'
          });
          this.centerDialogVisible = false
          this.loadPost()
          this. resetForm()
        }else{
          this.$message({
            message: '操作失败！',
            type: 'error'
          });
        }

      })
    },
    save(){
      this.$refs.form.validate((valid) => {
        if (valid) {
          if(this.form.id){
            this.doMod();
          }else{
            this.doSave();
          }
        } else {
          console.log('error submit!!');
          return false;
        }
      });

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
      this.$axios.post(this.$httpUrl+'/goods/listPage',{
        pageSize:this.pageSize,
        pageNum:this.pageNum,
        param:{
          name:this.name,
          goodstype:this.goodstype+'',
          storage:this.storage+''
        }
      }).then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){
          this.tableData=res.data
          this.total=res.total
        }else{
          alert('获取数据失败')
        }

      })
    },
    loadStorage(){
      this.$axios.get(this.$httpUrl+'/storage/list').then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){
          this.storageData=res.data
        }else{
          alert('获取数据失败')
        }

      })
    },
    loadGoodstype(){
      this.$axios.get(this.$httpUrl+'/goodstype/list').then(res=>res.data).then(res=>{
        console.log(res)
        if(res.code==200){
          this.goodstypeData=res.data
        }else{
          alert('获取数据失败')
        }

      })
    },
    showComments(row) {
      this.currentRow = row;
      this.commentDialogVisible = true;
    },
    showImportDialog() {
      this.importDialogVisible = true;
    },
  },

  beforeMount() {
    this.loadGoodstype()
    this.loadStorage()
    this.loadPost()
  }
}
</script>

<style scoped>
/* 原有的样式保持不变 */

/* 添加评论对话框相关样式 */
.el-dialog__body {
  padding: 10px 20px;
}

/* 确保评论列表在对话框中正确显示 */
.comment-dialog >>> .el-dialog {
  margin-top: 5vh !important;
}
</style>