<template>
  <div id="app" class="app-container">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="header-content">
        <h1 class="app-title">📊 E-Commerce Data Warehouse</h1>
        <nav class="app-nav">
          <ul>
            <li><router-link to="/">Dashboard</router-link></li>
            <li><router-link to="/sales">Sales Analytics</router-link></li>
            <li><router-link to="/products">Product Insights</router-link></li>
            <li><router-link to="/sync">Sync Monitor</router-link></li>
          </ul>
        </nav>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="app-footer">
      <p>&copy; 2024 University of Windsor. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  console.log('App mounted successfully')
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin: 0;
}

.app-nav ul {
  display: flex;
  list-style: none;
  gap: 2rem;
  margin: 0;
  padding: 0;
}

.app-nav a {
  color: white;
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.3s;
}

.app-nav a:hover,
.app-nav a.router-link-active {
  opacity: 0.8;
  text-decoration: underline;
}

.app-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 2rem auto;
  padding: 0 2rem;
}

.app-footer {
  background-color: #2c3e50;
  color: white;
  text-align: center;
  padding: 1.5rem;
  margin-top: auto;
}

@media (max-width: 768px) {
  .app-title {
    font-size: 1.4rem;
  }

  .app-nav ul {
    gap: 1rem;
    font-size: 0.9rem;
  }

  .app-main {
    margin: 1rem auto;
    padding: 0 1rem;
  }
}
</style>
