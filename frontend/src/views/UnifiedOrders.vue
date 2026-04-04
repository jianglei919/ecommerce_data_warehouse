<template>
  <div class="unified-orders-container">
    <h1>Unified Orders Management (App + Web)</h1>
    
    <!-- Overview Statistics -->
    <div class="overview-stats">
      <div class="stat-card">
        <div class="stat-value">{{ overview.totalOrders }}</div>
        <div class="stat-label">Total Orders</div>
      </div>
      <div class="stat-card app">
        <div class="stat-value">{{ overview.appOrders }}</div>
        <div class="stat-label">App Orders</div>
      </div>
      <div class="stat-card web">
        <div class="stat-value">{{ overview.webOrders }}</div>
        <div class="stat-label">Web Orders</div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters">
      <a-space>
        <a-select 
          v-model:value="filters.source" 
          placeholder="Select Source" 
          style="width: 150px"
          @change="handleFilterChange"
        >
          <a-select-option value="">All Sources</a-select-option>
          <a-select-option value="APP">App Source</a-select-option>
          <a-select-option value="WEB">Web Source</a-select-option>
        </a-select>
        
        <a-select 
          v-model:value="filters.status" 
          placeholder="Select Status" 
          style="width: 150px"
          @change="handleFilterChange"
        >
          <a-select-option value="">All Status</a-select-option>
          <a-select-option value="pending">Pending</a-select-option>
          <a-select-option value="completed">Completed</a-select-option>
          <a-select-option value="cancelled">Cancelled</a-select-option>
        </a-select>

        <a-button type="primary" @click="loadOrders">Refresh</a-button>
      </a-space>
    </div>

    <!-- Orders Table -->
    <div class="orders-table">
      <a-table
        :columns="columns"
        :data-source="orders"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="unifiedOrderId"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderNo'">
            {{ record.appOrderId || record.webOrderNo }}
          </template>
          <template v-else-if="column.key === 'source'">
            <a-tag :color="record.source === 'APP' ? 'blue' : 'green'">
              {{ record.source }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.status }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'totalAmount'">
            ¥{{ record.totalAmount.toFixed(2) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="showDetail(record)">View Details</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- Order Details Modal -->
    <a-modal
      v-model:visible="detailModal.visible"
      title="Order Details"
      width="800px"
      :footer="null"
    >
      <div v-if="detailModal.order" class="order-detail">
        <a-descriptions bordered :column="2" size="small">
          <a-descriptions-item label="Order ID">
            {{ detailModal.order.unifiedOrderId }}
          </a-descriptions-item>
          <a-descriptions-item label="Data Source">
            <a-tag :color="detailModal.order.source === 'APP' ? 'blue' : 'green'">
              {{ detailModal.order.source }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="Order No.">
            {{ detailModal.order.appOrderId || detailModal.order.webOrderNo }}
          </a-descriptions-item>
          <a-descriptions-item label="Order Date">
            {{ detailModal.order.orderDate }}
          </a-descriptions-item>
          <a-descriptions-item label="User ID">
            {{ detailModal.order.userId }}
          </a-descriptions-item>
          <a-descriptions-item label="Order Status">
            <a-tag :color="getStatusColor(detailModal.order.status)">
              {{ detailModal.order.status }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="Total Amount" :span="2">
            <span style="color: #ff4d4f; font-weight: bold;">
              ¥{{ detailModal.order.totalAmount.toFixed(2) }}
            </span>
          </a-descriptions-item>
        </a-descriptions>

        <!-- Order Items Details -->
        <div style="margin-top: 20px;">
          <h4>Order Items</h4>
          <a-table
            :columns="itemColumns"
            :data-source="detailModal.items"
            :pagination="false"
            size="small"
            rowKey="unifiedItemId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'subtotal'">
                ¥{{ record.subtotal.toFixed(2) }}
              </template>
              <template v-else-if="column.key === 'unitPrice'">
                ¥{{ record.unitPrice.toFixed(2) }}
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

interface UnifiedOrder {
  unifiedOrderId: number
  source: string
  appOrderId: number | null
  webOrderNo: string | null
  userId: number
  orderDate: string
  totalAmount: number
  status: string
  createdAt: string
  updatedAt: string
}

interface UnifiedOrderItem {
  unifiedItemId: number
  unifiedOrderId: number
  productId: number
  productName: string
  category: string
  quantity: number
  unitPrice: number
  subtotal: number
}

const API_BASE = 'http://localhost:8080/api'

const loading = ref(false)
const orders = ref<UnifiedOrder[]>([])
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0
})

const filters = ref({
  source: '',
  status: ''
})

const overview = ref({
  totalOrders: 0,
  appOrders: 0,
  webOrders: 0
})

const detailModal = ref({
  visible: false,
  order: null as UnifiedOrder | null,
  items: [] as UnifiedOrderItem[]
})

const columns = [
  {
    title: 'Order ID',
    dataIndex: 'unifiedOrderId',
    key: 'unifiedOrderId',
    width: 100
  },
  {
    title: 'Source',
    dataIndex: 'source',
    key: 'source',
    width: 80
  },
  {
    title: 'Order No.',
    key: 'orderNo',
    width: 150
  },
  {
    title: 'Order Date',
    dataIndex: 'orderDate',
    key: 'orderDate',
    width: 120
  },
  {
    title: 'Order Amount',
    dataIndex: 'totalAmount',
    key: 'totalAmount',
    width: 120,
    align: 'right'
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: 'Action',
    key: 'action',
    width: 100
  }
]

const itemColumns = [
  {
    title: 'Product ID',
    dataIndex: 'productId',
    key: 'productId',
    width: 80
  },
  {
    title: 'Product Name',
    dataIndex: 'productName',
    key: 'productName',
    width: 200
  },
  {
    title: 'Category',
    dataIndex: 'category',
    key: 'category',
    width: 100
  },
  {
    title: 'Quantity',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 60
  },
  {
    title: 'Unit Price',
    dataIndex: 'unitPrice',
    key: 'unitPrice',
    width: 100,
    align: 'right'
  },
  {
    title: 'Subtotal',
    dataIndex: 'subtotal',
    key: 'subtotal',
    width: 100,
    align: 'right'
  }
]

const getStatusColor = (status: string): string => {
  const colorMap: { [key: string]: string } = {
    'pending': 'orange',
    'completed': 'green',
    'cancelled': 'red'
  }
  return colorMap[status] || 'default'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      source: filters.value.source || undefined,
      status: filters.value.status || undefined,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize
    }

    const response = await axios.get(`${API_BASE}/unified-orders`, { params })
    orders.value = response.data.data
    pagination.value = response.data.pagination
  } catch (error) {
    console.error('Failed to load orders:', error)
  } finally {
    loading.value = false
  }
}

const loadOverview = async () => {
  try {
    const response = await axios.get(`${API_BASE}/unified-orders/overview`)
    overview.value = response.data
  } catch (error) {
    console.error('Failed to load overview:', error)
  }
}

const handleFilterChange = () => {
  pagination.value.current = 1
  loadOrders()
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadOrders()
}

const showDetail = async (order: UnifiedOrder) => {
  try {
    console.log('Loading order detail:', order.unifiedOrderId)
    const response = await axios.get(`${API_BASE}/unified-orders/${order.unifiedOrderId}`)
    console.log('Order detail response:', response.data)
    
    if (response.data && response.data.order) {
      detailModal.value.order = response.data.order
      detailModal.value.items = response.data.items || []
      detailModal.value.visible = true
    } else {
      console.error('Invalid response structure:', response.data)
    }
  } catch (error: any) {
    console.error('Failed to load order detail:', error.message)
    if (error.response) {
      console.error('Error response:', error.response.data)
    }
  }
}

onMounted(() => {
  loadOverview()
  loadOrders()
})
</script>

<style scoped>
.unified-orders-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

h1 {
  color: #333;
  margin-bottom: 20px;
  font-size: 24px;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card.app {
  border-left: 4px solid #1890ff;
}

.stat-card.web {
  border-left: 4px solid #52c41a;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.filters {
  background: white;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.orders-table {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  padding: 12px;
}

.order-detail {
  padding: 16px 0;
}

h4 {
  margin-top: 20px;
  margin-bottom: 12px;
  color: #333;
}
</style>
