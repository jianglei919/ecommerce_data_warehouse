<template>
  <div class="return-rate-chart">
    <div ref="chartRef" style="width: 100%; height: 300px;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chart = null

const initChart = () => {
  if (!chartRef.value) return
  
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const option = {
    title: {
      text: '商品类别退货率分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.componentSubType === 'pie') {
          return `${params.name}<br/>退货率: ${params.value}%`
        }
      }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '退货率',
        type: 'pie',
        radius: [0, 100],
        center: ['40%', '50%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 8
        },
        data: [
          { value: 2.5, name: '手机' },
          { value: 3.2, name: '电脑' },
          { value: 1.8, name: '配件' },
          { value: 4.1, name: '平板' },
          { value: 2.1, name: '可穿戴' }
        ]
      }
    ]
  }

  chart.setOption(option)
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => {
    chart?.resize()
  })
})
</script>

<style scoped>
.return-rate-chart {
  width: 100%;
}
</style>
