import axios, { AxiosInstance } from 'axios'

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 10000,
})

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('API Error:', error.message)
    return Promise.reject(error)
  }
)

export const analyticsApi = {
  // 获取按分类的销售统计
  getSalesByCategory: (startDate?: string, endDate?: string) =>
    apiClient.get('/api/analytics/sales/by-category', {
      params: { startDate, endDate },
    }),

  // 获取热门商品
  getTopRatedProducts: (limit: number = 10) =>
    apiClient.get('/api/analytics/products/top-rated', {
      params: { limit },
    }),

  // 获取同步统计
  getSyncStatistics: () =>
    apiClient.get('/api/analytics/sync/statistics'),

  // 发送测试订单
  sendTestOrder: (orderData: any) =>
    apiClient.post('/api/analytics/test/send-order', orderData),

  // 健康检查
  health: () =>
    apiClient.get('/api/analytics/health'),
}

export default apiClient
