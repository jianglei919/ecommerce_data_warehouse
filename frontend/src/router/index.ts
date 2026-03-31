import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

// 导入页面组件
import Dashboard from '../views/Dashboard.vue'
import SalesAnalytics from '../views/SalesAnalytics.vue'
import ProductInsights from '../views/ProductInsights.vue'
import SyncMonitor from '../views/SyncMonitor.vue'
import UnifiedOrders from '../views/UnifiedOrders.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
  },
  {
    path: '/orders',
    name: 'UnifiedOrders',
    component: UnifiedOrders,
  },
  {
    path: '/sales',
    name: 'SalesAnalytics',
    component: SalesAnalytics,
  },
  {
    path: '/products',
    name: 'ProductInsights',
    component: ProductInsights,
  },
  {
    path: '/sync',
    name: 'SyncMonitor',
    component: SyncMonitor,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
