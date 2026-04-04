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
import { message } from 'ant-design-vue'
import { analyticsApi } from '../api/analytics'
import dayjs, { Dayjs } from 'dayjs'

const analyticsChartRef = ref()
const dateRange = ref<[Dayjs, Dayjs] | null>(null)
const salesData = ref<any[]>([]) // 初始化为空，从API加载真实数据

const columns = [
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Sales', dataIndex: 'sales', key: 'sales' },
  { title: 'Percentage', dataIndex: 'percentage', key: 'percentage' },
  { title: 'Orders', dataIndex: 'orders', key: 'orders' },
]

const pagination = { pageSize: 10 }

const barColors = ['#667eea', '#52c41a', '#faad14', '#f5222d']

const initChart = (categories: string[] = ['Electronics', 'Wearables', 'Audio', 'Accessories'], 
                   salesData: number[] = [35000, 25000, 20000, 15000],
                   ordersData: number[] = [450, 320, 280, 200]) => {
  const chartDom = analyticsChartRef.value
  if (!chartDom) return

  const myChart = echarts.init(chartDom)

  const option = {
    tooltip: { 
      trigger: 'axis',
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach((param: any) => {
          result += `${param.marker} ${param.seriesName}: ${param.value.toLocaleString()}<br/>`
        })
        return result
      }
    },
    legend: { data: ['Sales ($)', 'Orders (#)'] },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
    },
    yAxis: [
      { 
        type: 'value',
        name: 'Sales ($)',
        axisLabel: { formatter: '${value}' }
      },
      { 
        type: 'value',
        name: 'Orders (#)',
        position: 'right',
        axisLabel: { formatter: '{value}' }
      }
    ],
    series: [
      {
        name: 'Sales ($)',
        type: 'bar',
        data: salesData,
        yAxisIndex: 0,
        itemStyle: {
          color: (params: any) => barColors[params.dataIndex % barColors.length]
        },
      },
      {
        name: 'Orders (#)',
        type: 'line',
        data: ordersData,
        yAxisIndex: 1,
        itemStyle: { color: '#764ba2' },
        smooth: true,
      },
    ],
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

const updateChart = () => {
  const categories = salesData.value.map(item => item.category)
  const sales = salesData.value.map(item => item.rawSales || 0)
  const orders = salesData.value.map(item => item.orders)
  initChart(categories, sales, orders)
}

const loadSalesData = async () => {
  try {
    let startDate: string | undefined
    let endDate: string | undefined
    
    if (dateRange.value && Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      startDate = dateRange.value[0].format('YYYY-MM-DD')
      endDate = dateRange.value[1].format('YYYY-MM-DD')
      
      // 验证日期范围有效性
      if (startDate > endDate) {
        message.warning('Start date must be before end date')
        return
      }
    } else {
      // 如果没有选择日期，提示用户需要选择
      message.warning('Please select a date range first')
      return
    }
    
    // 调用API获取数据
    const response = await analyticsApi.getSalesByCategory(startDate, endDate)
    
    // 如果有返回数据，则更新salesData
    if (response && response.data && response.data.length > 0) {
      const totalSales = response.data.reduce((sum: number, item: any) => sum + (item.total_sales_amount || 0), 0)
      
      salesData.value = response.data.map((item: any, index: number) => ({
        key: String(index),
        category: item.category || `Category ${index + 1}`,
        sales: item.total_sales_amount ? `$${(item.total_sales_amount).toFixed(2)}` : '$0',
        rawSales: item.total_sales_amount || 0,
        percentage: totalSales > 0 ? parseInt(((item.total_sales_amount / totalSales) * 100).toString()) : 0,
        orders: item.order_count || 0,
      }))
      updateChart()
      message.success('Data loaded successfully')
    } else {
      // 清空表格和图表
      salesData.value = []
      initChart()
      message.info('No data available for the selected date range')
    }
  } catch (error) {
    console.error('Failed to load sales data:', error)
    message.error('Failed to load sales data')
  }
}

onMounted(() => {
  initChart()
  // 页面加载时只初始化图表，不自动加载数据
  // 等待用户选择日期范围后点击"Load Data"按钮
})
</script>

<style scoped>
.analytics-container {
  padding: 20px;
}
</style>
