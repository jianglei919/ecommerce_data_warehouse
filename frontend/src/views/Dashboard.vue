<template>
  <div class="dashboard-container">
    <a-row :gutter="[16, 16]">
      <!-- 顶部统计卡片 -->
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <template #title>
            <span class="stat-title">📈 Total Sales</span>
          </template>
          <div class="stat-value">$125,430</div>
          <div class="stat-change">↑ 12.5% from last week</div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <template #title>
            <span class="stat-title">📦 Total Orders</span>
          </template>
          <div class="stat-value">2,840</div>
          <div class="stat-change">↑ 8.1% from last week</div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <template #title>
            <span class="stat-title">⭐ Avg Rating</span>
          </template>
          <div class="stat-value">4.6/5</div>
          <div class="stat-change">Based on 1,245 reviews</div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <template #title>
            <span class="stat-title">🔄 Sync Status</span>
          </template>
          <div class="stat-value" :style="{ color: syncStatus === 'active' ? '#52c41a' : '#f5222d' }">
            {{ syncStatus === 'active' ? 'Active' : 'Inactive' }}
          </div>
          <div class="stat-change">Real-time sync enabled</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 24px">
      <!-- 销售趋势图 -->
      <a-col :xs="24" :md="12">
        <a-card title="📊 Sales Trend">
          <div class="chart-container">
            <div id="sales-chart" ref="salesChartRef" style="width: 100%; height: 300px"></div>
          </div>
        </a-card>
      </a-col>

      <!-- 分类销售占比 -->
      <a-col :xs="24" :md="12">
        <a-card title="🎯 Sales by Category">
          <div class="chart-container">
            <div id="category-chart" ref="categoryChartRef" style="width: 100%; height: 300px"></div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 测试订单按钮 -->
    <a-row :gutter="[16, 16]" style="margin-top: 24px">
      <a-col :xs="24">
        <a-card title="🧪 Test ETL Pipeline">
          <a-space>
            <a-button type="primary" @click="sendTestOrder">Send Test Order</a-button>
            <a-button @click="refreshData">Refresh Data</a-button>
            <span v-if="lastTestTime" class="test-info">
              Last test: {{ lastTestTime }}
            </span>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import { analyticsApi } from '../api/analytics'
import dayjs from 'dayjs'

const salesChartRef = ref()
const categoryChartRef = ref()
const syncStatus = ref('active')
const lastTestTime = ref('')

// 初始化销售趋势图
const initSalesChart = () => {
  const chartDom = salesChartRef.value
  if (!chartDom) return

  const myChart = echarts.init(chartDom)

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Jan 15', 'Jan 16', 'Jan 17', 'Jan 18', 'Jan 19', 'Jan 20', 'Jan 21'],
    },
    yAxis: { type: 'value' },
    series: [
      {
        data: [12000, 15200, 18900, 16500, 21000, 19800, 22500],
        type: 'line',
        smooth: true,
        itemStyle: { color: '#667eea' },
        areaStyle: { color: 'rgba(102, 126, 234, 0.1)' },
      },
    ],
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

// 初始化分类销售图
const initCategoryChart = () => {
  const chartDom = categoryChartRef.value
  if (!chartDom) return

  const myChart = echarts.init(chartDom)

  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: 'Sales',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 35000, name: 'Electronics' },
          { value: 25000, name: 'Wearables' },
          { value: 20000, name: 'Audio' },
          { value: 15000, name: 'Accessories' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

// 发送测试订单
const sendTestOrder = async () => {
  try {
    const testOrder = {
      eventType: 'ORDER_CREATED',
      source: 'APP',
      userId: Math.floor(Math.random() * 5) + 1,
      orderDate: dayjs().format('YYYY-MM-DD'),
      totalAmount: Math.random() * 500 + 50,
      itemCount: Math.floor(Math.random() * 5) + 1,
    }

    await analyticsApi.sendTestOrder(testOrder)
    message.success('Test order sent successfully!')
    lastTestTime.value = dayjs().format('HH:mm:ss')
  } catch (error) {
    message.error('Failed to send test order')
    console.error(error)
  }
}

// 刷新数据
const refreshData = async () => {
  try {
    await analyticsApi.getSyncStatistics()
    message.success('Data refreshed!')
  } catch (error) {
    message.error('Failed to refresh data')
    console.error(error)
  }
}

onMounted(() => {
  initSalesChart()
  initCategoryChart()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  border-left: 4px solid #667eea;
  border-radius: 8px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-title {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #2c3e50;
  margin: 10px 0;
}

.stat-change {
  font-size: 0.85rem;
  color: #999;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.test-info {
  color: #667eea;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
}
</style>
