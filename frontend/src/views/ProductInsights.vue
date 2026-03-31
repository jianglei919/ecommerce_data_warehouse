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
import { analyticsApi } from '../api/analytics'

const productsChartRef = ref()
const limit = ref(10)
const topProducts = ref([
  {
    key: '1',
    productId: 'P001',
    name: 'Wireless Earbuds Pro',
    category: 'Audio',
    rating: 4.8,
    reviews: 245,
    status: 'Active',
    sales: 1200,
  },
  {
    key: '2',
    productId: 'P002',
    name: 'Smart Watch X',
    category: 'Wearables',
    rating: 4.6,
    reviews: 189,
    status: 'Active',
    sales: 950,
  },
  {
    key: '3',
    productId: 'P003',
    name: '4K Webcam',
    category: 'Electronics',
    rating: 4.5,
    reviews: 156,
    status: 'Active',
    sales: 780,
  },
  {
    key: '4',
    productId: 'P004',
    name: 'USB-C Hub',
    category: 'Accessories',
    rating: 4.3,
    reviews: 134,
    status: 'Active',
    sales: 650,
  },
])

const columns = [
  { title: 'Product', dataIndex: 'name', key: 'name' },
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Rating', dataIndex: 'rating', key: 'rating' },
  { title: 'Reviews', dataIndex: 'reviews', key: 'reviews' },
  { title: 'Sales', dataIndex: 'sales', key: 'sales' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
]

const pagination = { pageSize: 10 }

const initChart = () => {
  const chartDom = productsChartRef.value
  if (!chartDom) return

  const myChart = echarts.init(chartDom)

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Product A', 'Product B', 'Product C', 'Product D', 'Product E'],
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Rating Score',
        type: 'bar',
        data: [4.8, 4.6, 4.5, 4.3, 4.2],
        itemStyle: { color: '#52c41a' },
      },
    ],
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

const loadTopProducts = async () => {
  try {
    await analyticsApi.getTopRatedProducts(limit.value)
  } catch (error) {
    console.error('Failed to load top products:', error)
  }
}

onMounted(() => {
  initChart()
})
</script>

<style scoped>
.insights-container {
  padding: 20px;
}
</style>
