<template>
  <div class="insights-container">
    <a-card title="⭐ Product Insights">
      <a-row :gutter="[16, 16]" style="margin-bottom: 20px">
        <a-col :xs="24" :sm="12">
          <a-input-number
            v-model:value="limit"
            :min="1"
            :max="100"
            placeholder="Number of products"
            style="width: 100%"
          />
        </a-col>
        <a-col :xs="24" :sm="12">
          <a-button type="primary" @click="loadTopProducts" style="width: 100%">
            Load Top Products
          </a-button>
        </a-col>
      </a-row>

      <div id="products-chart" ref="productsChartRef" style="width: 100%; height: 400px"></div>

      <a-table
        :columns="columns"
        :data-source="topProducts"
        :pagination="pagination"
        style="margin-top: 20px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'rating'">
            <a-rate :value="record.rating" :allow-half="true" :disabled="true" />
            <span style="margin-left: 10px">{{ record.rating }}/5</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'Active' ? 'green' : 'red'">
              {{ record.status }}
            </a-tag>
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

const productsChartRef = ref()
const limit = ref(10)
const barColors = ['#667eea', '#52c41a', '#faad14', '#f5222d', '#13c2c2']
const topProducts = ref<any[]>([])

const columns = [
  { title: 'Product', dataIndex: 'name', key: 'name' },
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Brand', dataIndex: 'brand', key: 'brand' },
  { title: 'Rating', dataIndex: 'rating', key: 'rating' },
  { title: 'Sales ($)', dataIndex: 'totalSalesAmount', key: 'totalSalesAmount' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
]

const pagination = { pageSize: 10 }

const initChart = (productNames: string[] = [], ratings: number[] = []) => {
  const chartDom = productsChartRef.value
  if (!chartDom) return

  const myChart = echarts.init(chartDom)

  const option = {
    tooltip: { 
      trigger: 'axis',
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach((param: any) => {
          result += `${param.marker} ${param.seriesName}: ${param.value.toFixed(1)}<br/>`
        })
        return result
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: productNames.length > 0 ? productNames : ['No Data'],
    },
    yAxis: { 
      type: 'value',
      name: 'Rating',
      min: 0,
      max: 5,
    },
    series: [
      {
        name: 'Rating Score',
        type: 'bar',
        data: ratings.length > 0 ? ratings : [],
        itemStyle: {
          color: (params: any) => barColors[params.dataIndex % barColors.length]
        },
      },
    ],
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

const loadTopProducts = async () => {
  try {
    const response = await analyticsApi.getTopRatedProducts(limit.value)
    
    if (response && response.data && response.data.length > 0) {
      // 转换API响应数据格式
      topProducts.value = response.data.map((item: any, index: number) => ({
        key: String(index + 1),
        productId: item.productId || `P${String(index + 1).padStart(3, '0')}`,
        name: item.name || `Product ${index + 1}`,
        category: item.category || 'Other',
        brand: item.brand || 'Unknown',
        rating: item.rating || 0,
        reviews: item.salesCount || 0,
        totalSalesAmount: `$${item.totalSalesAmount ? item.totalSalesAmount.toFixed(2) : '0.00'}`,
        sales: item.totalSalesAmount || 0,
        status: item.status || 'Active',
      }))

      // 更新图表
      const productNames = topProducts.value.map(p => p.name)
      const ratings = topProducts.value.map(p => p.rating)
      initChart(productNames, ratings)
      
      message.success('Top products loaded successfully')
    } else {
      message.info('No products data available')
    }
  } catch (error) {
    console.error('Failed to load top products:', error)
    message.error('Failed to load top products')
  }
}

onMounted(() => {
  initChart()
  loadTopProducts()
})
</script>

<style scoped>
.insights-container {
  padding: 20px;
}
</style>
