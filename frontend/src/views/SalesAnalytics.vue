<template>
  <div class="analytics-container">
    <a-card title="📊 Sales Analytics">
      <a-row :gutter="[16, 16]" style="margin-bottom: 20px">
        <a-col :xs="24" :sm="12">
          <a-date-picker
            v-model:value="dateRange"
            type="dateRange"
            placeholder="Select date range"
            style="width: 100%"
          />
        </a-col>
        <a-col :xs="24" :sm="12">
          <a-button type="primary" @click="loadSalesData" style="width: 100%">
            Load Data
          </a-button>
        </a-col>
      </a-row>

      <div id="sales-analytics-chart" ref="analyticsChartRef" style="width: 100%; height: 400px"></div>

      <a-table
        :columns="columns"
        :data-source="salesData"
        :pagination="pagination"
        style="margin-top: 20px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'percentage'">
            <a-progress :percent="record.percentage" />
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { analyticsApi } from '../api/analytics'
import dayjs, { Dayjs } from 'dayjs'

const analyticsChartRef = ref()
const dateRange = ref<[Dayjs, Dayjs] | null>(null)
const salesData = ref([
  {
    key: '1',
    category: 'Electronics',
    sales: '35,000',
    percentage: 35,
    orders: 450,
  },
  {
    key: '2',
    category: 'Wearables',
    sales: '25,000',
    percentage: 25,
    orders: 320,
  },
  {
    key: '3',
    category: 'Audio',
    sales: '20,000',
    percentage: 20,
    orders: 280,
  },
  {
    key: '4',
    category: 'Accessories',
    sales: '15,000',
    percentage: 15,
    orders: 200,
  },
])

const columns = [
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Sales', dataIndex: 'sales', key: 'sales' },
  { title: 'Percentage', dataIndex: 'percentage', key: 'percentage' },
  { title: 'Orders', dataIndex: 'orders', key: 'orders' },
]

const pagination = { pageSize: 10 }

const initChart = () => {
  const chartDom = analyticsChartRef.value
  if (!chartDom) return

  const myChart = echarts.init(chartDom)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['Sales', 'Orders'] },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Electronics', 'Wearables', 'Audio', 'Accessories'],
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Sales',
        type: 'bar',
        data: [35000, 25000, 20000, 15000],
        itemStyle: { color: '#667eea' },
      },
      {
        name: 'Orders',
        type: 'line',
        data: [450, 320, 280, 200],
        itemStyle: { color: '#764ba2' },
      },
    ],
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

const loadSalesData = async () => {
  try {
    const startDate = dateRange.value?.[0].format('YYYY-MM-DD')
    const endDate = dateRange.value?.[1].format('YYYY-MM-DD')
    await analyticsApi.getSalesByCategory(startDate, endDate)
  } catch (error) {
    console.error('Failed to load sales data:', error)
  }
}

onMounted(() => {
  initChart()
})
</script>

<style scoped>
.analytics-container {
  padding: 20px;
}
</style>
