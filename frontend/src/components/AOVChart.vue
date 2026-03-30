<template>
  <div class="aov-chart">
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

  const dates = []
  const aovData = []
  
  // 生成过去30天的数据
  for (let i = 29; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN'))
    aovData.push(1800 + Math.random() * 400)
  }

  const option = {
    title: {
      text: '30天AOV趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (params.length) {
          return `${params[0].name}<br/>AOV: ¥${params[0].value.toFixed(2)}`
        }
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '平均订单价值(¥)'
    },
    series: [
      {
        data: aovData,
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#FF6B6B'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 107, 107, 0.3)' },
            { offset: 1, color: 'rgba(255, 107, 107, 0)' }
          ])
        }
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
.aov-chart {
  width: 100%;
}
</style>
