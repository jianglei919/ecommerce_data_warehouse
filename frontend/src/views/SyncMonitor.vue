<template>
  <div class="sync-container">
    <a-card title="🔄 Sync Monitor">
      <a-row :gutter="[16, 16]" style="margin-bottom: 20px">
        <a-col :xs="24" :sm="8">
          <a-statistic title="Total Synced" :value="syncStats.totalSynced" />
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-statistic title="Success Rate" :value="syncStats.successRate" suffix="%" />
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-statistic title="Last Sync" :value="syncStats.lastSync" />
        </a-col>
      </a-row>

      <a-divider />

      <a-row :gutter="[16, 16]">
        <a-col :xs="24">
          <a-button type="primary" @click="refreshSyncStatus">
            Refresh Status
          </a-button>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="syncLogs"
        :pagination="pagination"
        :loading="loading"
        style="margin-top: 20px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'source'">
            <a-tag :color="record.source === 'APP' ? 'blue' : 'orange'">
              {{ record.source }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { analyticsApi } from '../api/analytics'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const loading = ref(false)
const syncStats = ref({
  totalSynced: 2840,
  successRate: 99.8,
  lastSync: 'now',
})

const syncLogs = ref([
  {
    key: '1',
    eventId: 'EVT-001',
    eventType: 'ORDER_CREATED',
    source: 'APP',
    orderId: '1001',
    status: 'SUCCESS',
    timestamp: dayjs().subtract(5, 'minutes'),
  },
  {
    key: '2',
    eventId: 'EVT-002',
    eventType: 'ORDER_UPDATED',
    source: 'WEB',
    orderId: 'WEB-2024-001',
    status: 'SUCCESS',
    timestamp: dayjs().subtract(10, 'minutes'),
  },
  {
    key: '3',
    eventId: 'EVT-003',
    eventType: 'ORDER_CREATED',
    source: 'APP',
    orderId: '1003',
    status: 'FAILED',
    timestamp: dayjs().subtract(15, 'minutes'),
  },
  {
    key: '4',
    eventId: 'EVT-004',
    eventType: 'ORDER_CREATED',
    source: 'WEB',
    orderId: 'WEB-2024-002',
    status: 'SUCCESS',
    timestamp: dayjs().subtract(20, 'minutes'),
  },
])

const columns = [
  { title: 'Event ID', dataIndex: 'eventId', key: 'eventId' },
  { title: 'Event Type', dataIndex: 'eventType', key: 'eventType' },
  { title: 'Source', dataIndex: 'source', key: 'source' },
  { title: 'Order ID', dataIndex: 'orderId', key: 'orderId' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
  {
    title: 'Timestamp',
    dataIndex: 'timestamp',
    key: 'timestamp',
    customRender: ({ text }: any) => text.fromNow(),
  },
]

const pagination = { pageSize: 20 }

const getStatusColor = (status: string) => {
  const colorMap: { [key: string]: string } = {
    SUCCESS: 'green',
    FAILED: 'red',
    PROCESSING: 'processing',
  }
  return colorMap[status] || 'default'
}

const refreshSyncStatus = async () => {
  loading.value = true
  try {
    const result = await analyticsApi.getSyncStatistics()
    if (result.data) {
      syncStats.value.totalSynced = result.data.total_synced || 2840
      syncStats.value.lastSync = 'just now'
    }
  } catch (error) {
    console.error('Failed to refresh sync status:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 初始化时获取数据
  refreshSyncStatus()

  // 每30秒自动刷新
  setInterval(() => {
    refreshSyncStatus()
  }, 30000)
})
</script>

<style scoped>
.sync-container {
  padding: 20px;
}
</style>
