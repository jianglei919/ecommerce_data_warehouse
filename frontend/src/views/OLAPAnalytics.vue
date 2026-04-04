<template>
  <div class="olap-analytics-container">
    <a-card title="� OLAP Analytics Dashboard" style="border-radius: 12px;">
      <template #extra>
        <span style="font-size: 12px; color: #666;">Real-time Data Analysis</span>
      </template>
      <a-tabs v-model:activeKey="activeTab" type="card" size="large">
        <!-- Rollup Tab -->
        <a-tab-pane key="rollup" tab="📈 Rollup (Monthly Aggregation)">
          <div class="controls-card">
            <a-row :gutter="[24, 24]">
              <a-col :xs="24" :sm="12">
                <div class="controls-label">Category Filter</div>
                <a-select
                  v-model:value="rollupCategory"
                  placeholder="Select category (optional)"
                  allow-clear
                  @change="fetchRollupData"
                >
                  <a-select-option value="">All Categories</a-select-option>
                  <a-select-option value="Electronics">Electronics</a-select-option>
                  <a-select-option value="Computers">Computers</a-select-option>
                  <a-select-option value="Accessories">Accessories</a-select-option>
                  <a-select-option value="Furniture">Furniture</a-select-option>
                </a-select>
              </a-col>
              <a-col :xs="24" :sm="12">
                <div class="controls-label">Year</div>
                <a-input-number
                  v-model:value="rollupYear"
                  placeholder="Enter year"
                  @change="fetchRollupData"
                  :min="2020"
                  :max="2030"
                />
              </a-col>
            </a-row>
          </div>
          <div class="data-card">
            <a-table
              :columns="rollupColumns"
              :data-source="rollupData"
              :loading="rollupLoading"
              :pagination="false"
              size="small"
            />
          </div>
        </a-tab-pane>

        <!-- Drilldown Tab -->
        <a-tab-pane key="drilldown" tab="🔎 Drilldown (Product Detail)">
          <div class="controls-card">
            <a-row :gutter="[24, 24]">
              <a-col :xs="24" :sm="8">
                <div class="controls-label">Category</div>
                <a-select
                  v-model:value="drilldownCategory"
                  placeholder="Select category"
                  @change="fetchDrilldownData"
                >
                  <a-select-option value="Electronics">Electronics</a-select-option>
                  <a-select-option value="Computers">Computers</a-select-option>
                  <a-select-option value="Accessories">Accessories</a-select-option>
                  <a-select-option value="Furniture">Furniture</a-select-option>
                </a-select>
              </a-col>
              <a-col :xs="24" :sm="8">
                <div class="controls-label">Year</div>
                <a-input-number
                  v-model:value="drilldownYear"
                  placeholder="Enter year"
                  @change="fetchDrilldownData"
                  :min="2020"
                  :max="2030"
                />
              </a-col>
              <a-col :xs="24" :sm="8">
                <div class="controls-label">Month (Optional)</div>
                <a-input-number
                  v-model:value="drilldownMonth"
                  placeholder="1-12 (optional)"
                  @change="fetchDrilldownData"
                  :min="1"
                  :max="12"
                />
              </a-col>
            </a-row>
          </div>
          <div class="data-card">
            <a-table
              :columns="drilldownColumns"
              :data-source="drilldownData"
              :loading="drilldownLoading"
              :pagination="{ pageSize: 10 }"
              size="small"
            />
          </div>
        </a-tab-pane>

        <!-- Slice Tab -->
        <a-tab-pane key="slice" tab="🔀 Slice (Category Trend)">
          <div class="controls-card">
            <a-row :gutter="[24, 24]">
              <a-col :xs="24" :sm="12">
                <div class="controls-label">Category</div>
                <a-select
                  v-model:value="sliceCategory"
                  placeholder="Select category"
                  @change="fetchSliceData"
                >
                  <a-select-option value="Electronics">Electronics</a-select-option>
                  <a-select-option value="Computers">Computers</a-select-option>
                  <a-select-option value="Accessories">Accessories</a-select-option>
                  <a-select-option value="Furniture">Furniture</a-select-option>
                </a-select>
              </a-col>
              <a-col :xs="24" :sm="12">
                <div class="controls-label">Year</div>
                <a-input-number
                  v-model:value="sliceYear"
                  placeholder="Enter year"
                  @change="fetchSliceData"
                  :min="2020"
                  :max="2030"
                />
              </a-col>
            </a-row>
          </div>
          <div class="chart-container">
            <div ref="sliceChartRef" style="width: 100%; height: 250px;"></div>
          </div>
          <div class="data-card">
            <a-table
              :columns="sliceColumns"
              :data-source="sliceData"
              :loading="sliceLoading"
              :pagination="{ pageSize: 15 }"
              size="small"
            />
          </div>
        </a-tab-pane>

        <!-- Dice Tab -->
        <a-tab-pane key="dice" tab="🎲 Dice (Multi-Dimensional Filter)">
          <div class="controls-card">
            <a-row :gutter="[24, 24]">
              <a-col :xs="24" :sm="8">
                <div class="controls-label">Categories</div>
                <a-select
                  v-model:value="diceCategories"
                  mode="multiple"
                  placeholder="Select categories"
                  allow-clear
                  :max-tag-count="2"
                  style="width: 100%"
                  @change="fetchDiceData"
                >
                  <a-select-option key="Electronics" value="Electronics">Electronics</a-select-option>
                  <a-select-option key="Computers" value="Computers">Computers</a-select-option>
                  <a-select-option key="Accessories" value="Accessories">Accessories</a-select-option>
                  <a-select-option key="Furniture" value="Furniture">Furniture</a-select-option>
                </a-select>
              </a-col>
              <a-col :xs="24" :sm="8">
                <div class="controls-label">Year</div>
                <a-input-number
                  v-model:value="diceYear"
                  placeholder="Enter year"
                  @change="fetchDiceData"
                  :min="2020"
                  :max="2030"
                />
              </a-col>
              <a-col :xs="24" :sm="8">
                <div class="controls-label">Months</div>
                <a-select
                  v-model:value="diceMonths"
                  mode="multiple"
                  placeholder="Select months"
                  allow-clear
                  :max-tag-count="2"
                  style="width: 100%"
                  @change="fetchDiceData"
                >
                  <a-select-option key="1" value="1">January</a-select-option>
                  <a-select-option key="2" value="2">February</a-select-option>
                  <a-select-option key="3" value="3">March</a-select-option>
                  <a-select-option key="4" value="4">April</a-select-option>
                  <a-select-option key="5" value="5">May</a-select-option>
                  <a-select-option key="6" value="6">June</a-select-option>
                  <a-select-option key="7" value="7">July</a-select-option>
                  <a-select-option key="8" value="8">August</a-select-option>
                  <a-select-option key="9" value="9">September</a-select-option>
                  <a-select-option key="10" value="10">October</a-select-option>
                  <a-select-option key="11" value="11">November</a-select-option>
                  <a-select-option key="12" value="12">December</a-select-option>
                </a-select>
              </a-col>
            </a-row>
          </div>
          <div class="data-card">
            <a-table
              :columns="diceColumns"
              :data-source="diceData"
              :loading="diceLoading"
              :pagination="false"
              size="small"
            />
          </div>
        </a-tab-pane>

        <!-- Pivot Tab -->
        <a-tab-pane key="pivot" tab="🔄 Pivot (Month × Category)">
          <div class="controls-card">
            <a-row :gutter="[24, 24]">
              <a-col :xs="24" :sm="12">
                <div class="controls-label">Year</div>
                <a-input-number
                  v-model:value="pivotYear"
                  placeholder="Enter year"
                  @change="fetchPivotData"
                  :min="2020"
                  :max="2030"
                />
              </a-col>
              <a-col :xs="24" :sm="12">
                <div class="controls-label">Action</div>
                <a-button type="primary" @click="fetchPivotData" style="width: 100%">
                  🔄 Refresh Data
                </a-button>
              </a-col>
            </a-row>
          </div>
          <div class="chart-container">
            <div ref="pivotChartRef" style="width: 100%; height: 400px;" />
          </div>
          <div class="data-card">
            <a-table
              :columns="pivotColumns"
              :data-source="pivotData"
              :loading="pivotLoading"
              :pagination="false"
              size="small"
              :scroll="{ x: 1200 }"
            />
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'

// Rollup State
const rollupCategory = ref<string | null>(null)
const rollupYear = ref<number | null>(2024)
const rollupData = ref([])
const rollupLoading = ref(false)
const monthNames = ['', 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']

const rollupColumns = [
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Year', dataIndex: 'year', key: 'year' },
  { title: 'Month', dataIndex: 'monthName', key: 'month' },
  { title: 'Quantity', dataIndex: 'monthly_qty', key: 'qty' },
  { title: 'Sales', dataIndex: 'monthly_sales', key: 'sales', render: (val:number) => `$${val.toFixed(2)}` },
]

// Drilldown State
const drilldownCategory = ref<string>('Electronics')
const drilldownYear = ref<number>(2024)
const drilldownMonth = ref<number | null>(null)
const drilldownData = ref([])
const drilldownLoading = ref(false)
const drilldownColumns = [
  { title: 'Product', dataIndex: 'product_name', key: 'product' },
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Date', dataIndex: 'formatDate', key: 'date' },
  { title: 'Quantity', dataIndex: 'total_quantity', key: 'qty' },
  { title: 'Amount', dataIndex: 'total_sales_amount', key: 'amount', render: (val:number) => `$${val.toFixed(2)}` },
]

// Slice State
const sliceChartRef = ref()
const sliceCategory = ref<string>('Electronics')
const sliceYear = ref<number>(2024)
const sliceData = ref([])
const sliceLoading = ref(false)
const sliceColumns = [
  { title: 'Date', dataIndex: 'formatDate', key: 'date' },
  { title: 'Quantity', dataIndex: 'total_quantity', key: 'qty' },
  { title: 'Sales', dataIndex: 'total_sales_amount', key: 'sales', render: (val:number) => `$${val.toFixed(2)}` },
]

// Dice State
const diceCategories = ref<string[]>(['Electronics', 'Accessories'])
const diceYear = ref<number>(2024)
const diceMonths = ref<string[]>(['1', '2'])
const diceData = ref([])
const diceLoading = ref(false)
const diceColumns = [
  { title: 'Category', dataIndex: 'category', key: 'category' },
  { title: 'Year', dataIndex: 'year', key: 'year' },
  { title: 'Month', dataIndex: 'monthName', key: 'month' },
  { title: 'Quantity', dataIndex: 'qty', key: 'qty' },
  { title: 'Sales', dataIndex: 'sales', key: 'sales', render: (val:number) => `$${val.toFixed(2)}` },
]

// Pivot State
const pivotYear = ref<number>(2024)
const pivotChartRef = ref()
const pivotData = ref<any[]>([])
const pivotLoading = ref(false)
const pivotColumns = ref<any[]>([])

// Active Tab
const activeTab = ref('rollup')

// Fetch Functions
const fetchRollupData = async () => {
  rollupLoading.value = true
  try {
    let url = '/api/unified-orders/analytics/rollup'
    const params = new URLSearchParams()
    if (rollupCategory.value) params.append('category', rollupCategory.value)
    if (rollupYear.value) params.append('year', String(rollupYear.value))
    if (params.toString()) url += `?${params.toString()}`

    const response = await fetch(url)
    const result = await response.json()
    rollupData.value = result.data.map((item: any, idx: number) => ({
      ...item,
      key: idx,
      monthName: monthNames[item.month] || `Month ${item.month}`
    }))
  } catch (error) {
    message.error('Failed to load rollup data')
    console.error(error)
  } finally {
    rollupLoading.value = false
  }
}

const fetchDrilldownData = async () => {
  drilldownLoading.value = true
  try {
    let url = '/api/unified-orders/analytics/drilldown'
    const params = new URLSearchParams()
    if (drilldownCategory.value) params.append('category', drilldownCategory.value)
    if (drilldownYear.value) params.append('year', String(drilldownYear.value))
    if (drilldownMonth.value) params.append('month', String(drilldownMonth.value))
    if (params.toString()) url += `?${params.toString()}`

    const response = await fetch(url)
    const result = await response.json()
    drilldownData.value = result.data.map((item: any, idx: number) => ({
      ...item,
      key: idx,
      formatDate: `${item.year}-${String(item.month).padStart(2, '0')}-${String(item.day).padStart(2, '0')}`
    }))
  } catch (error) {
    message.error('Failed to load drilldown data')
    console.error(error)
  } finally {
    drilldownLoading.value = false
  }
}

const fetchSliceData = async () => {
  sliceLoading.value = true
  try {
    let url = '/api/unified-orders/analytics/slice'
    const params = new URLSearchParams()
    if (sliceCategory.value) params.append('category', sliceCategory.value)
    if (sliceYear.value) params.append('year', String(sliceYear.value))
    if (params.toString()) url += `?${params.toString()}`

    const response = await fetch(url)
    const result = await response.json()
    sliceData.value = result.data.map((item: any, idx: number) => ({
      ...item,
      key: idx,
      formatDate: `${item.year}-${String(item.month).padStart(2, '0')}-${String(item.day).padStart(2, '0')}`
    }))
    
    // Update chart
    await updateSliceChart()
  } catch (error) {
    message.error('Failed to load slice data')
    console.error(error)
  } finally {
    sliceLoading.value = false
  }
}

const fetchDiceData = async () => {
  diceLoading.value = true
  try {
    let url = '/api/unified-orders/analytics/dice'
    const params = new URLSearchParams()
    if (diceCategories.value.length) params.append('categories', diceCategories.value.join(','))
    if (diceYear.value) params.append('year', String(diceYear.value))
    if (diceMonths.value.length) params.append('months', diceMonths.value.join(','))
    if (params.toString()) url += `?${params.toString()}`

    const response = await fetch(url)
    const result = await response.json()
    diceData.value = result.data.map((item: any, idx: number) => ({
      ...item,
      key: idx,
      monthName: monthNames[item.month] || `Month ${item.month}`
    }))
  } catch (error) {
    message.error('Failed to load dice data')
    console.error(error)
  } finally {
    diceLoading.value = false
  }
}

const fetchPivotData = async () => {
  pivotLoading.value = true
  try {
    let url = '/api/unified-orders/analytics/pivot'
    const params = new URLSearchParams()
    if (pivotYear.value) params.append('year', String(pivotYear.value))
    if (params.toString()) url += `?${params.toString()}`

    const response = await fetch(url)
    const result = await response.json()
    pivotData.value = result.data.map((item: any, idx: number) => ({ ...item, key: idx }))

    // Generate dynamic columns
    pivotColumns.value = [
      { title: 'Category', dataIndex: 'category', key: 'category' },
      { title: 'Jan', dataIndex: 'Jan_Sales', key: 'jan', render: (val:number) => `$${val.toFixed(2)}` },
      { title: 'Feb', dataIndex: 'Feb_Sales', key: 'feb', render: (val:number) => `$${val.toFixed(2)}` },
      { title: 'Mar', dataIndex: 'Mar_Sales', key: 'mar', render: (val:number) => `$${val.toFixed(2)}` },
      { title: 'Apr', dataIndex: 'Apr_Sales', key: 'apr', render: (val:number) => `$${val.toFixed(2)}` },
      { title: 'Total', dataIndex: 'Total_Sales', key: 'total', render: (val:number) => `$${val.toFixed(2)}` },
    ]

    // Update chart
    await updatePivotChart()
  } catch (error) {
    message.error('Failed to load pivot data')
    console.error(error)
  } finally {
    pivotLoading.value = false
  }
}

const updateSliceChart = async () => {
  await nextTick()
  
  if (!sliceChartRef.value || sliceData.value.length === 0) return

  const chart = echarts.init(sliceChartRef.value)
  const dates = sliceData.value.map((item: any) => `${item.month}-${item.day}`)
  const sales = sliceData.value.map((item: any) => item.total_sales_amount)

  chart.setOption({
    title: { text: `${sliceCategory.value} Sales Trend` },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ data: sales, type: 'line', smooth: true }],
  })
}

const updatePivotChart = async () => {
  await nextTick()
  
  if (!pivotChartRef.value || pivotData.value.length === 0) return
  
  const chart = echarts.init(pivotChartRef.value)
  
  // Extract months and categories
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  const categories = pivotData.value.map((item: any) => item.category)
  
  // Build series for each month
  const series = months.map(month => ({
    name: month,
    data: pivotData.value.map((item: any) => item[`${month}_Sales`] || 0),
    type: 'bar',
  }))
  
  chart.setOption({
    title: { text: 'Monthly Sales by Category' },
    tooltip: { trigger: 'axis' },
    legend: { data: months },
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value', name: 'Sales ($)' },
    series: series,
  })
}

// Load data on mount
onMounted(() => {
  fetchRollupData()
  fetchDrilldownData()
  fetchSliceData()
  fetchDiceData()
  fetchPivotData()
})

// Watch for Slice tab activation to ensure chart renders
watch(activeTab, async (newVal) => {
  if (newVal === 'slice') {
    await nextTick()
    await updateSliceChart()
  }
  if (newVal === 'pivot') {
    await nextTick()
    await updatePivotChart()
  }
})
</script>

<style scoped>
.olap-analytics-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

:deep(.ant-card) {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

:deep(.ant-card-head) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
  padding: 16px 24px;
  font-size: 18px;
  font-weight: 600;
}

:deep(.ant-tabs) {
  background: white;
  border-radius: 12px;
}

:deep(.ant-tabs-tab-btn) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #667eea;
}

:deep(.ant-tabs-ink-bar) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.controls-card {
  background: #f9fafb;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 24px;
  border: 1px solid #e5e7eb;
}

.controls-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:deep(.ant-select) {
  width: 100%;
}

:deep(.ant-input-number) {
  width: 100%;
}

.data-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  margin-top: 12px;
}

.chart-container {
  background: white;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

:deep(.ant-table) {
  font-size: 13px;
}

:deep(.ant-table-thead > tr > th) {
  background: #f3f4f6 !important;
  font-weight: 600;
  color: #374151;
}

:deep(.ant-button-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 500;
}

:deep(.ant-button-primary:hover) {
  opacity: 0.9;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .olap-analytics-container {
    padding: 16px;
  }
  
  .controls-card {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .data-card {
    padding: 16px;
    margin-top: 8px;
  }
}
</style>
