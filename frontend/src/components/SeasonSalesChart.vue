<template>
  <div class="season-sales-chart">
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
      text: '按季节和类别分析销量',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['Spring', 'Summer', 'Fall', 'Winter'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: ['电子', '服装', '食品', '图书', '运动']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'Spring',
        data: [320, 200, 250, 180, 220],
        type: 'bar',
        stack: 'total'
      },
      {
        name: 'Summer',
        data: [420, 310, 280, 220, 260],
        type: 'bar',
        stack: 'total'
      },
      {
        name: 'Fall',
        data: [280, 250, 320, 260, 240],
        type: 'bar',
        stack: 'total'
      },
      {
        name: 'Winter',
        data: [380, 280, 200, 300, 280],
        type: 'bar',
        stack: 'total'
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
.season-sales-chart {
  width: 100%;
}
</style>
