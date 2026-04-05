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
  totalSynced: 0,
  successRate: 0,
  lastSync: 'Loading...',
})

const syncLogs = ref<any[]>([])

const columns = [
  { title: 'Event ID', dataIndex: 'eventId', key: 'eventId', width: 150 },
  { title: 'Event Type', dataIndex: 'eventType', key: 'eventType' },
  { title: 'Source', dataIndex: 'source', key: 'source' },
  { title: 'Order ID', dataIndex: 'orderId', key: 'orderId' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
  { title: 'Error Message', dataIndex: 'errorMessage', key: 'errorMessage', width: 300 },
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
    // 并行获取统计数据和日志数据
    const [statsResult, logsResult] = await Promise.all([
      analyticsApi.getSyncStatistics(),
      analyticsApi.getSyncLogs(100),
    ])

    // 更新统计数据
    if (statsResult.data) {
      syncStats.value.totalSynced = statsResult.data.total_synced || 0
      syncStats.value.successRate = statsResult.data.success_rate || 0
      syncStats.value.lastSync = statsResult.data.last_sync_time || 'Never'
    }

    // 更新日志数据
    if (logsResult.data && Array.isArray(logsResult.data)) {
      syncLogs.value = logsResult.data.map((log: any, index: number) => ({
        key: String(index + 1),
        eventId: log.eventId,
        eventType: log.eventType,
        source: log.source,
        orderId: log.orderId,
        status: log.status,
        timestamp: log.timestamp ? dayjs(log.timestamp) : dayjs(),
        errorMessage: log.errorMessage,
      }))
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
