<template>
  <div>
    <el-dialog :title="title" :visible="localVisible" @close="handleClose" width="650px">
      <el-form :model="form" ref="form" label-width="120px">
<!--        <el-form-item label="导入类型" prop="importType">
          <el-select v-model="form.importType" placeholder="请选择导入类型">
            <el-option label="数据资产" value="goods"></el-option>
&lt;!&ndash;            <el-option label="入库操作" value="records"></el-option>&ndash;&gt;
          </el-select>
        </el-form-item>-->
        
        <el-form-item label="目标仓库" prop="storage" required>
          <el-select v-model="form.storage" placeholder="请选择仓库">
            <el-option
                v-for="item in storageList"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="资产分类" prop="goodstype" required>
          <el-select v-model="form.goodstype" placeholder="请选择资产分类">
            <el-option
                v-for="item in typeList"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="Excel文件" prop="file" :rules="[{required: true, message:'请选择要导入的Excel文件'}]">
          <el-upload
              ref="upload"
              class="upload-demo"
              :action="uploadAction"
              :headers="uploadHeaders"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :on-success="onUploadSuccess"
              :on-error="onUploadError"
              :on-change="handleFileChange"
              :before-upload="beforeUpload"
              :auto-upload="false"
              :file-list="fileList"
              :limit="1">
            <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
            <el-button 
                size="small" 
                type="success" 
                @click="submitUpload" 
                :loading="uploading">
                {{ uploading ? '上传中' : '开始导入' }}
            </el-button>
            <div slot="tip" class="el-upload__tip">只能上传xlsx/xls文件</div>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <div class="download-template">
            <el-button type="text" @click="downloadTemplate">下载导入模板</el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <div v-if="importResult.success" class="import-result">
        <el-alert
            title="导入成功"
            type="success"
            :description="`成功导入 ${importResult.successCount} 条数据，共 ${importResult.total} 条`"
            show-icon>
        </el-alert>
      </div>
      
      <div v-if="importResult.errors && importResult.errors.length > 0" class="import-errors">
        <el-alert
            title="部分数据导入失败"
            type="warning"
            show-icon>
        </el-alert>
        <el-collapse v-model="activeCollapseNames">
          <el-collapse-item title="查看错误详情" name="1">
            <ul class="error-list">
              <li v-for="(error, index) in importResult.errors" :key="index">{{ error }}</li>
            </ul>
          </el-collapse-item>
        </el-collapse>
      </div>
      
      <div v-if="importErrors && importErrors.length > 0" class="error-details">
        <p class="error-title">错误详情：</p>
        <el-collapse>
          <el-collapse-item title="展开查看详细错误信息" name="1">
            <ul class="error-list">
              <li v-for="(error, index) in importErrors" :key="index" class="error-item">
                {{ error }}
              </li>
            </ul>
          </el-collapse-item>
        </el-collapse>
      </div>
      
      <el-progress 
        v-if="uploading" 
        :percentage="uploadProgress"
        :status="validUploadStatus">
      </el-progress>
      
      <div v-if="importResult.success" class="import-statistics">
        <h4>导入结果统计</h4>
        <el-table :data="[importResult]" border style="width: 100%">
          <el-table-column prop="total" label="总数" width="120"></el-table-column>
          <el-table-column prop="successCount" label="成功" width="120"></el-table-column>
          <el-table-column prop="failCount" label="失败" width="120"></el-table-column>
        </el-table>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "ImportDataDialog",
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    importType: {
      type: String,
      default: 'goods' // 'goods' 或 'records'
    },
    title: {
      type: String,
      default: '导入数据'
    },
    showTypeSelect: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      localVisible: this.visible, // 本地状态跟踪对话框是否可见
      form: {
        importType: 'goods',
        storage: null,
        goodstype: null
      },
      storageList: [],
      typeList: [],
      fileList: [],
      uploading: false,
      importResult: {},
      activeCollapseNames: [],
      uploadProgress: 0,
      uploadStatus: '',
      uploadTimer: null,
      uploadCompleted: false,
      uploadHeaders: {
        Authorization: sessionStorage.getItem('token') || ''
      },
      importErrors: []
    }
  },
  computed: {
    uploadAction() {
      const baseUrl = this.$httpUrl;
      const type = this.form.importType || 'goods';
      
      // 添加日志以便调试
      console.log('下载模板类型:', type);
      console.log('模板下载URL:', `${baseUrl}/import/template/${type}`);
      
      return `${baseUrl}/import/${type}?storage=${this.form.storage || ''}&goodstype=${this.form.goodstype || ''}`;
    },
    validUploadStatus() {
      const validStatuses = ['success', 'exception', 'warning'];
      return validStatuses.includes(this.uploadStatus) ? this.uploadStatus : undefined;
    }
  },
  watch: {
    visible(val) {
      this.localVisible = val;
      if (val) {
        this.loadWarehousesAndTypes();
        this.resetForm();
      }
    },
    importType(val) {
      this.form.importType = val;
    }
  },
  methods: {
    // 处理对话框关闭
    handleClose() {
      this.$emit('update:visible', false);
    },
    resetForm() {
      this.form = {
        importType: 'goods',
        storage: null,
        goodstype: null
      };
      this.fileList = [];
      this.importResult = {};
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles();
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields();
      }
    },
    loadWarehousesAndTypes() {
      this.$axios.get(this.$httpUrl + '/storage/list').then(res => res.data).then(res => {
        if (res.code == 200) {
          this.storageList = res.data;
        } else {
          this.$message.error('获取仓库数据失败');
        }
      }).catch(err => {
        console.error('加载仓库失败:', err);
        this.$message.error('加载仓库失败');
      });
      
      this.$axios.get(this.$httpUrl + '/goodstype/list').then(res => res.data).then(res => {
        if (res.code == 200) {
          this.typeList = res.data;
        } else {
          this.$message.error('获取分类数据失败');
        }
      }).catch(err => {
        console.error('加载分类失败:', err);
        this.$message.error('加载分类失败');
      });
    },
    handleRemove(file, fileList) {
      this.fileList = fileList;
    },
    handlePreview() {
      // 不使用参数，但保留方法
      console.log('预览文件');
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.ms-excel' || 
                      file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      const isLt2M = file.size / 1024 / 1024 < 10;

      if (!isExcel) {
        this.$message.error('只能上传Excel文件!');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('文件大小不能超过10MB!');
        return false;
      }
      return true;
    },
    handleFileChange(file, fileList) {
      this.fileList = fileList;
    },
    submitUpload() {
      if (!this.$refs.upload.uploadFiles || this.$refs.upload.uploadFiles.length === 0) {
        this.$message.warning("请先选择导入的文件");
        return;
      }
      
      this.uploading = true;
      this.uploadProgress = 0;
      this.uploadStatus = '';
      
      this.uploadTimer = setInterval(() => {
        if (this.uploadProgress < 90) {
          this.uploadProgress += 5;
        }
      }, 300);
      
      this.$refs.upload.submit();
      
      console.log('开始上传文件:', this.$refs.upload.uploadFiles[0].name);
    },
    onUploadSuccess(response, file) {
      clearInterval(this.uploadTimer);
      this.uploadProgress = 100;
      this.uploading = false;
      
      console.log('上传响应:', response);
      console.log('上传的文件:', file);
      
      // 根据响应结果设置状态
      if (response.code === 200) {
        this.uploadStatus = 'success';
        this.$message.success('导入成功！');
        
        // 显示导入结果信息
        const result = `成功导入 ${response.data.successCount}/${response.data.total} 条数据`;
        this.importResult = result;
        
        // 如果存在部分失败记录，显示错误详情
        if (response.data.errors && response.data.errors.length > 0) {
          this.importErrors = response.data.errors;
          this.$message.warning(`部分数据导入失败，共 ${response.data.errors.length} 条错误`);
        }
        
        // 可选：关闭对话框并刷新父组件列表
        setTimeout(() => {
          this.$emit('success');
        }, 1500);
      } else {
        this.uploadStatus = 'exception';
        this.importResult = '导入失败: ' + (response.msg || '未知错误');
        
        // 显示错误详情
        if (response.data && response.data.errors && response.data.errors.length > 0) {
          this.importErrors = response.data.errors;
        }
        
        this.$message.error(this.importResult);
      }
    },
    onUploadError(err, file) {
      clearInterval(this.uploadTimer);
      this.uploadProgress = 100;
      this.uploadStatus = 'exception';
      this.uploading = false;
      
      console.error('上传失败，错误详情:', err);
      console.error('上传的文件:', file);
      
      let errorMsg = '文件上传失败';
      
      // 尝试提取更详细的错误信息
      if (err && err.message) {
        errorMsg += ': ' + err.message;
      } else if (typeof err === 'string') {
        errorMsg += ': ' + err;
      } else if (err && err.response && err.response.data) {
        // 尝试读取响应体中的错误信息
        const responseData = err.response.data;
        errorMsg += ': ' + (responseData.msg || responseData.message || JSON.stringify(responseData));
      }
      
      this.importResult = errorMsg;
      this.$message.error(errorMsg);
    },
    downloadTemplate() {
      const baseUrl = this.$httpUrl;
      const type = this.form.importType || 'goods';
      
      // 添加日志以便调试
      console.log('下载模板类型:', type);
      console.log('模板下载URL:', `${baseUrl}/import/template/${type}`);
      
      window.open(`${baseUrl}/import/template/${type}`);
    }
  }
}
</script>

<style scoped>
.import-result {
  margin-top: 20px;
}
.import-errors {
  margin-top: 20px;
}
.error-list {
  color: #F56C6C;
  margin-top: 10px;
  padding-left: 20px;
}
.import-statistics {
  margin-top: 20px;
}
.error-details {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  background-color: #fef0f0;
}
.error-title {
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 5px;
}
.error-item {
  line-height: 20px;
  color: #606266;
}
.download-template {
  margin-top: 10px;
  margin-bottom: 10px;
}
</style>
