<template>
  <div class="dashboard">
    <el-row :gutter="20" class="dashboard-header">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="kpi-card">
          <div class="kpi-title">总订单数</div>
          <div class="kpi-value">1,250</div>
          <div class="kpi-trend">📈 +12% 本周</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="kpi-card">
          <div class="kpi-title">总销售额</div>
          <div class="kpi-value">¥250,000</div>
          <div class="kpi-trend">📈 +8% 本周</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="kpi-card">
          <div class="kpi-title">平均订单价值</div>
          <div class="kpi-value">¥2,000</div>
          <div class="kpi-trend">📊 稳定</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="kpi-card">
          <div class="kpi-title">退货率</div>
          <div class="kpi-value">3.2%</div>
          <div class="kpi-trend">📉 -0.5% 本周</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-content">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>🔥 热销商品分析</span>
              <div class="button-group">
                <el-button 
                  v-for="tab in productTabs" 
                  :key="tab.value"
                  :type="activeProductTab === tab.value ? 'primary' : 'info'"
                  @click="activeProductTab = tab.value"
                  size="small">
                  {{ tab.label }}
                </el-button>
              </div>
            </div>
          </template>
          <HotProductChart :tab="activeProductTab" />
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>📅 季节销售分析</span>
          </template>
          <SeasonSalesChart />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-content">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>💰 平均订单价值趋势</span>
          </template>
          <AOVChart />
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>📦 退货率分析</span>
          </template>
          <ReturnRateChart />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import HotProductChart from '../components/HotProductChart.vue'
import SeasonSalesChart from '../components/SeasonSalesChart.vue'
import AOVChart from '../components/AOVChart.vue'
import ReturnRateChart from '../components/ReturnRateChart.vue'

const activeProductTab = ref('sales')
const productTabs = [
  { label: '按销量', value: 'sales' },
  { label: '按评分', value: 'rating' },
  { label: '综合排名', value: 'combined' },
]
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.dashboard-header {
  margin-bottom: 20px;
}

.kpi-card {
  text-align: center;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
}

.kpi-title {
  color: #606266;
  font-size: 14px;
  margin-bottom: 10px;
}

.kpi-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.kpi-trend {
  color: #909399;
  font-size: 12px;
}

.dashboard-content {
  margin-bottom: 20px;
}

.chart-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.button-group {
  display: flex;
  gap: 8px;
}
</style>
