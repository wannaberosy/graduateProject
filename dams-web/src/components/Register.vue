<template>
  <div class="registerBody">
    <div class="registerDiv">
      <div class="register-content">
        <h1 class="register-title">用户注册</h1>
        <el-form :model="registerForm" label-width="100px"
                 :rules="rules" ref="registerForm">
          <el-form-item label="账号" prop="no">
            <el-input style="width: 200px" type="text" v-model="registerForm.no"
                      autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item label="用户名" prop="name">
            <el-input style="width: 200px" type="text" v-model="registerForm.name"
                      autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input style="width: 200px" type="password" v-model="registerForm.password"
                      show-password autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input style="width: 200px" type="password" v-model="registerForm.confirmPassword"
                      show-password autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="registerForm.sex">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input-number 
              v-model="registerForm.age" 
              :min="1" 
              :max="120" 
              size="small"
              style="width: 200px">
            </el-input-number>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input style="width: 200px" type="text" v-model="registerForm.phone"
                      autocomplete="off" size="small"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="register" :disabled="register_disabled">注 册</el-button>
            <el-button @click="goToLogin">返回登录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    // 自定义验证规则：确认密码
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    return {
      register_disabled: false,
      registerForm: {
        no: '',
        name: '',
        password: '',
        confirmPassword: '',
        sex: 1,  // 默认选中男性
        age: 18, // 默认年龄
        phone: '',
        roleId: 2  // 默认为普通用户
      },
      rules: {
        no: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ],
        sex: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        age: [
          { required: true, message: '请输入年龄', trigger: 'blur' },
          { type: 'number', min: 1, max: 120, message: '年龄必须在1-120岁之间', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    goToLogin() {
      this.$router.push('/');
    },
    register() {
      this.register_disabled = true;
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          // 创建要提交的用户数据对象
          const userData = {
            no: this.registerForm.no,
            name: this.registerForm.name,
            password: this.registerForm.password,
            sex: this.registerForm.sex,
            age: this.registerForm.age,
            phone: this.registerForm.phone,
            roleId: this.registerForm.roleId
          };

          // 发送注册请求
          this.$axios.post(this.$httpUrl + '/user/register', userData)
            .then(res => {
              if (res.data.code === 200) {
                this.$message.success('注册成功！');
                // 注册成功后跳转到登录页
                setTimeout(() => {
                  this.$router.push('/');
                }, 1500);
              } else {
                this.$message.error(res.data.msg || '注册失败，请重试！');
                this.register_disabled = false;
              }
            })
            .catch(error => {
              console.error('注册失败:', error);
              this.$message.error('注册失败，请重试！');
              this.register_disabled = false;
            });
        } else {
          this.register_disabled = false;
          return false;
        }
      });
    }
  }
}
</script>

<style scoped>
.registerBody {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: #B3C0D1;
}
.registerDiv {
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -300px;  /* 增加高度以适应新增的字段 */
  margin-left: -250px;
  width: 450px;
  height: 600px;  /* 增加高度以适应新增的字段 */
  background: #fff;
  border-radius: 5%;
}
.register-title {
  margin: 20px 0;
  text-align: center;
}
.register-content {
  width: 400px;
  height: 550px;  /* 增加高度以适应新增的字段 */
  position: absolute;
  top: 25px;
  left: 25px;
}
.el-button {
  margin-right: 10px;
}
/* 添加性别单选框的样式 */
.el-radio-group {
  margin-left: 20px;
}
/* 调整年龄输入框的样式 */
.el-input-number {
  width: 200px;
}
</style>