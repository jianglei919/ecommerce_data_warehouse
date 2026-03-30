import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

export const productAPI = {
  // 获取热销商品（按销量）
  getTopProductsBySales: () => api.get('/products/top-sales'),
  
  // 获取热销商品（按评论和评分）
  getTopProductsByReviews: () => api.get('/products/top-reviews'),
  
  // 获取综合热销商品
  getCombinedTopProducts: () => api.get('/products/combined-top'),
}

export const salesAPI = {
  // 获取季节销售分析
  getSalesBySeason: () => api.get('/sales/by-season'),
  
  // 按类别分析销量
  getSalesByCategory: () => api.get('/sales/by-category'),
}

export const analyticsAPI = {
  // 获取平均订单价值
  getAOV: () => api.get('/analytics/aov'),
  
  // 获取退货率
  getReturnRate: () => api.get('/analytics/return-rate'),
  
  // 获取日KPI
  getDailyKPI: () => api.get('/analytics/daily-kpi'),
}

export default api
