<script>
  export default {
    name:"Aside",
    data(){
      return{
      }
    },

    computed:{
      "menu":{
        get(){
          return this.$store.state.menu
        }
      }
    },
    props:{
      isCollapse:Boolean
    }
  }
</script>

<template>
  <el-menu
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b"
      default-active="/Home"
      style="height: 200vh;"
      :collapse="isCollapse"
      :collapse-transition="false"
      router
  >

    <el-menu-item index="/Home">
      <i class="el-icon-s-home"></i>
      <span slot="title">首页</span>
    </el-menu-item>

    <el-menu-item :index="'/'+item.menuclick" v-for="(item,i) in menu" :key="i">
      <i :class="item.menuicon"></i>
      <span slot="title">{{item.menuname}}</span>
    </el-menu-item>

  </el-menu>
</template>

<style scoped>

</style>