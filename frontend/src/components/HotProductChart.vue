<template>
  <div class="hot-product-chart">
    <div ref="chartRef" style="width: 100%; height: 300px;"></div>
    <el-table :data="tableData" class="product-table" stripe>
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="salesQty" label="销量" width="80" align="right" />
      <el-table-column prop="avgRating" label="评分" width="80" align="right">
        <template #default="{ row }">
          <el-rate v-model="row.avgRating" disabled allow-half />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import * as echarts from 'echarts'
import { productAPI } from '../api'

const props = defineProps({
  tab: {
    type: String,
    default: 'sales'
  }
})

const chartRef = ref(null)
let chart = null
const tableData = ref([])

const initChart = () => {
  if (!chartRef.value) return
  
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const mockData = [
    { productName: 'iPhone 14 Pro', salesQty: 450, rating: 4.8 },
    { productName: 'MacBook Pro 16', salesQty: 320, rating: 4.9 },
    { productName: 'iPad Air', salesQty: 280, rating: 4.6 },
    { productName: 'AirPods Pro', salesQty: 560, rating: 4.7 },
    { productName: 'Apple Watch', salesQty: 390, rating: 4.5 },
  ]

  tableData.value = mockData

  const option = {
    title: {
      text: props.tab === 'sales' ? '按销量排序' : props.tab === 'rating' ? '按评分排序' : '综合排名',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: mockData.map(d => d.productName)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: mockData.map(d => d.salesQty),
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }

  chart.setOption(option)
}

watch(() => props.tab, () => {
  initChart()
})

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => {
    chart?.resize()
  })
})
</script>

<style scoped>
.hot-product-chart {
  width: 100%;
}

.product-table {
  margin-top: 20px;
}
</style>
