<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="logo">📊 电商数据仓库演示</div>
        <div class="title">E-Commerce Data Warehouse Demo</div>
      </el-header>
      <el-container>
        <el-main class="app-main">
          <router-view></router-view>
        </el-main>
      </el-container>
      <el-footer class="app-footer">
        <p>© 2024 E-Commerce Data Warehouse Demo. All rights reserved.</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style scoped>
.app-header {
  background-color: #282c34;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
}

.title {
  font-size: 14px;
  color: #bbb;
}

.app-main {
  min-height: calc(100vh - 120px);
  background-color: #f5f7fa;
  padding: 20px;
}

.app-footer {
  background-color: #282c34;
  color: white;
  text-align: center;
  padding: 20px;
}

.app-footer p {
  margin: 0;
  font-size: 12px;
}
</style>
