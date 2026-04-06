<template>
  <div class="data-input-container">
    <div class="layout">
      <!-- 左侧：输入区域 -->
      <div class="input-panel">
        <h2>📥 JSON Data Import</h2>

        <div class="controls">
          <div class="control-row">
            <label>Select Business DB *</label>
            <select v-model="selectedDb">
              <option value="">-- Please Select --</option>
              <option value="APP">APP DB</option>
              <option value="WEB">WEB DB</option>
            </select>
          </div>
        </div>

        <div class="textarea-wrapper">
          <label>Paste JSON Data</label>
          <textarea
            v-model="jsonInput" 
            placeholder="Paste single object or array..."
            class="json-input"
          ></textarea>
        </div>

        <div class="buttons">
          <button @click="submitData" :disabled="!jsonInput.trim() || !selectedDb" class="btn btn-primary">
            {{ isLoading ? 'Processing...' : '✓ Submit' }}
          </button>
          <button @click="validateJson" class="btn btn-validate">Validate JSON</button>
          <button @click="clearForm" class="btn btn-clear">Clear</button>
        </div>

        <!-- 消息反馈 -->
        <div v-if="successMessage" class="message success">
          ✓ {{ successMessage }}
        </div>
        <div v-if="errorMessage" class="message error">
          ✗ {{ errorMessage }}
        </div>
        <div v-if="validationMessage" class="message info">
          ℹ {{ validationMessage }}
        </div>
      </div>

      <!-- 右侧：模板 -->
      <div class="template-panel">
        <h2>📋 Data Templates</h2>

        <div class="template">
          <h3>Product (New)</h3>
          <pre>{{ productNewTemplate }}</pre>
          <button @click="copyToClipboard(productNewTemplate)" class="copy-btn">Copy</button>
        </div>

        <div class="template">
          <h3>Product (Edit)</h3>
          <pre>{{ productEditTemplate }}</pre>
          <button @click="copyToClipboard(productEditTemplate)" class="copy-btn">Copy</button>
        </div>

        <div class="template">
          <h3>Order (New)</h3>
          <pre>{{ orderNewTemplate }}</pre>
          <button @click="copyToClipboard(orderNewTemplate)" class="copy-btn">Copy</button>
        </div>

        <div class="template">
          <h3>Order (Edit)</h3>
          <pre>{{ orderEditTemplate }}</pre>
          <button @click="copyToClipboard(orderEditTemplate)" class="copy-btn">Copy</button>
        </div>

        <div class="instructions">
          <h4>📌 Instructions</h4>
          <ul>
            <li>New: ID field not required</li>
            <li>Edit: Must include ID field</li>
            <li>Batch: Send array [...]</li>
            <li>Auto-syncs to warehouse DB</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 最近操作记录 -->
    <div v-if="recentOperations.length > 0" class="history-section">
      <h3>📝 Recent Operations (Max 10)</h3>
      <div class="operation-list">
        <div v-for="op in recentOperations" :key="op.id" class="operation-item" :class="op.status">
          <span class="op-time">{{ op.time }}</span>
          <span class="op-type">[{{ op.type }}]</span>
          <span class="op-db">{{ op.db }}</span>
          <span class="op-status">{{ op.status === 'success' ? '✓' : '✗' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { API_BASE_URL } from '../api'

const selectedDb = ref('')
const dataType = ref('')
const jsonInput = ref('')
const isLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const validationMessage = ref('')
const recentOperations = ref<any[]>([])

const productTemplate = ref(`{
  "product_name": "iPhone 15 Pro",
  "category": "Electronics",
  "price": 999.99,
  "stock_quantity": 50
}

// Add ID when updating:
{
  "product_id": 1,
  "product_name": "iPhone 15 Pro",
  "category": "Electronics",
  "price": 999.99,
  "stock_quantity": 50
}

// Batch operation:
[
  {"product_name": "iPhone 15", "category": "Electronics", "price": 899.99, "stock_quantity": 100},
  {"product_name": "MacBook Pro", "category": "Electronics", "price": 1999.99, "stock_quantity": 30}
]`)

const orderTemplate = ref(`{
  "order_id": "ORD-2024-001",
  "user_id": 1,
  "product_id": 1,
  "total_amount": 999.99,
  "quantity": 1,
  "status": "PENDING",
  "remarks": "Order note"
}

// When updating (include order ID):
{
  "order_id": "ORD-2024-001",
  "user_id": 1,
  "product_id": 1,
  "total_amount": 999.99,
  "quantity": 1,
  "status": "SHIPPED"
}

// Batch operation:
[
  {"order_id": "ORD-2024-001", "user_id": 1, "product_id": 1, "total_amount": 999.99, "quantity": 1, "status": "PENDING"},
  {"order_id": "ORD-2024-002", "user_id": 2, "product_id": 2, "total_amount": 1999.99, "quantity": 1, "status": "PENDING"}
]`)

// Validate JSON format
const validateJson = () => {
  try {
    validationMessage.value = ''
    errorMessage.value = ''

    if (!jsonInput.value.trim()) {
      validationMessage.value = 'Please enter JSON data'
      return
    }

    const data = JSON.parse(jsonInput.value)
    const isArray = Array.isArray(data)
    const items = isArray ? data : [data]

    let detectedType = ''
    for (const item of items) {
      if (item.product_id !== undefined || item.product_name !== undefined) {
        detectedType = 'product'
        break
      }
      if (item.order_id !== undefined || (item.total_amount !== undefined && item.quantity !== undefined)) {
        detectedType = 'order'
        break
      }
    }

    const details = isArray
      ? `✓ Valid array, containing ${items.length} records`
      : `✓ Valid object`

    validationMessage.value = `${details} | Detected type: ${detectedType || 'Unknown'}`

    if (detectedType && !dataType.value) {
      dataType.value = detectedType
    }
  } catch (error) {
    validationMessage.value = ''
    errorMessage.value = `JSON format error: ${error instanceof Error ? error.message : 'Unknown error'}`
  }
}

// Copy template
const copyTemplate = (type: string) => {
  const template = type === 'product' ? productTemplate.value : orderTemplate.value
  navigator.clipboard.writeText(template).then(() => {
    successMessage.value = `Copied ${type === 'product' ? 'Product' : 'Order'} template`
    setTimeout(() => {
      successMessage.value = ''
    }, 2000)
  })
}

// Submit data
const submitData = async () => {
  try {
    isLoading.value = true
    successMessage.value = ''
    errorMessage.value = ''
    validationMessage.value = ''

    if (!selectedDb.value) {
      errorMessage.value = 'Please select a business DB'
      return
    }

    if (!jsonInput.value.trim()) {
      errorMessage.value = 'Please enter JSON data'
      return
    }

    // Validate JSON
    let data = JSON.parse(jsonInput.value)
    const isArray = Array.isArray(data)
    data = isArray ? data : [data]

    // Autodetect type
    let detectedType = dataType.value

    if (!detectedType) {
      for (const item of data) {
        if (item.product_id !== undefined || item.product_name !== undefined) {
          detectedType = 'product'
          break
        }
        if (item.order_id !== undefined || (item.total_amount !== undefined && item.quantity !== undefined)) {
          detectedType = 'order'
          break
        }
      }
    }

    if (!detectedType) {
      errorMessage.value = 'Unable to detect data type, please specify type or use standard template'
      return
    }

    // Call backend API
    const endpoint = detectedType === 'product' ? '/api/data/import/products' : '/api/data/import/orders'
    const payload = {
      db: selectedDb.value,
      data: data,
      dataType: detectedType
    }

    console.log(`📤 Submitting ${detectedType} data to ${selectedDb.value} DB:`, payload)

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    const result = await response.json()

    if (result.status === 'success') {
      successMessage.value = `✓ Successfully processed ${result.count} ${detectedType === 'product' ? 'product' : 'order'} records. Message sent to Kafka.`

      // Add to history
      recentOperations.value.unshift({
        id: Date.now(),
        time: new Date().toLocaleTimeString('en-US'),
        type: detectedType === 'product' ? 'Product' : 'Order',
        db: selectedDb.value,
        status: 'success'
      })

      if (recentOperations.value.length > 10) {
        recentOperations.value = recentOperations.value.slice(0, 10)
      }

      // Clear input
      jsonInput.value = ''
    } else {
      errorMessage.value = result.message || 'Data processing failed'
      recentOperations.value.unshift({
        id: Date.now(),
        time: new Date().toLocaleTimeString('en-US'),
        type: detectedType === 'product' ? 'Product' : 'Order',
        db: selectedDb.value,
        status: 'error'
      })
    }
  } catch (error) {
    errorMessage.value = `Error: ${error instanceof Error ? error.message : 'Unknown error'}`
    recentOperations.value.unshift({
      id: Date.now(),
      time: new Date().toLocaleTimeString('en-US'),
      type: 'Unknown',
      db: selectedDb.value,
      status: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

const clearForm = () => {
  jsonInput.value = ''
  dataType.value = ''
  validationMessage.value = ''
  errorMessage.value = ''
  successMessage.value = ''
}
</script>

<style scoped>
.data-input-container {
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.input-section, .template-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

h2 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  font-size: 1.4rem;
  border-bottom: 3px solid #667eea;
  padding-bottom: 0.5rem;
}

h3 {
  color: #667eea;
  margin-top: 1.5rem;
  font-size: 1.1rem;
}

/* 控制区域 */
.controls {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.control-group {
  display: flex;
  flex-direction: column;
}

.control-group label {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.4rem;
  font-size: 0.9rem;
}

.control-group select {
  padding: 0.6rem;
  border: 1px solid #e0e6ed;
  border-radius: 6px;
  font-size: 0.95rem;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.control-group select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* JSON输入 */
.json-input-wrapper {
  margin-bottom: 1.5rem;
}

.json-input-wrapper label {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.6rem;
  font-size: 0.95rem;
}

.json-textarea {
  width: 100%;
  height: 400px;
  padding: 1rem;
  border: 2px solid #e0e6ed;
  border-radius: 8px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 0.85rem;
  resize: vertical;
  transition: all 0.3s;
}

.json-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.hint {
  display: block;
  color: #96a1b2;
  margin-top: 0.4rem;
  font-size: 0.85rem;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

button {
  padding: 0.8rem 1.2rem;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-submit {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-clear {
  background: #f0f0f0;
  color: #2c3e50;
}

.btn-clear:hover {
  background: #e0e0e0;
}

.btn-validate {
  background: #fff3cd;
  color: #856404;
}

.btn-validate:hover {
  background: #ffeaa7;
}

/* 消息 */
.message {
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.message strong {
  display: block;
  margin-bottom: 0.4rem;
  font-size: 0.95rem;
}

.message p {
  margin: 0;
  font-size: 0.9rem;
}

.success-message {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.error-message {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.info-message {
  background: #d1ecf1;
  border: 1px solid #bee5eb;
  color: #0c5460;
}

/* 模板区域 */
.template-box {
  background: #f7f9fb;
  border: 1px solid #e0e6ed;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  overflow-x: auto;
}

.template-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95rem;
}

.copy-btn {
  background: #667eea;
  color: white;
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
  border-radius: 4px;
}

.copy-btn:hover {
  background: #5568d3;
}

.template-box pre {
  margin: 0;
  font-size: 0.8rem;
  color: #2c3e50;
  line-height: 1.4;
}

.template-box code {
  font-family: 'Monaco', 'Courier New', monospace;
  background: white;
  padding: 0;
}

.instructions {
  background: #f0f4ff;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.instructions ul {
  margin: 0.5rem 0 0 1.5rem;
  padding: 0;
}

.instructions li {
  margin-bottom: 0.6rem;
  font-size: 0.9rem;
  color: #2c3e50;
  line-height: 1.5;
}

.instructions code {
  background: white;
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 0.85rem;
}

/* 历史记录 */
.history-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.history-section h3 {
  margin-top: 0;
}

.operation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

.operation-item {
  padding: 1rem;
  background: #f7f9fb;
  border-radius: 8px;
  border-left: 4px solid #e0e6ed;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.operation-item.success {
  border-left-color: #28a745;
  background: #f0fdf4;
}

.operation-item.error {
  border-left-color: #dc3545;
  background: #fef2f2;
}

.op-time {
  color: #96a1b2;
  font-size: 0.8rem;
}

.op-type {
  font-weight: 600;
  color: #667eea;
}

.op-db {
  background: #667eea;
  color: white;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.op-status {
  margin-left: auto;
  font-weight: bold;
}

.operation-item.success .op-status {
  color: #28a745;
}

.operation-item.error .op-status {
  color: #dc3545;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .json-textarea {
    height: 300px;
  }
}
</style>
