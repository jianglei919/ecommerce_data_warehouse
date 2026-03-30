# Java Demo 项目 - 前端可视化设计

## 前端技术栈

```
框架: Vue.js 3
构建: Vite
UI库: Element Plus
图表: ECharts 5
HTTP: Axios
状态管理: Pinia（可选）
```

---

## 项目结构

```
ecommerce-warehouse-frontend/
├── package.json
├── vite.config.js
├── index.html
├── public/
├── src/
│   ├── api/
│   │   ├── product.js          # 商品API调用
│   │   ├── sales.js            # 销售API调用
│   │   └── analytics.js        # 分析API调用
│   │
│   ├── components/
│   │   ├── HotProductChart.vue       # 热销商品图表
│   │   ├── SeasonSalesChart.vue      # 季节销售图表
│   │   ├── AOVChart.vue              # AOV趋势图表
│   │   ├── ReturnRateChart.vue       # 退货率图表
│   │   └── KPICard.vue               # KPI卡片
│   │
│   ├── pages/
│   │   ├── Dashboard.vue       # 主仪表板
│   │   ├── ProductAnalysis.vue # 商品分析页
│   │   ├── SalesAnalysis.vue   # 销售分析页
│   │   └── NotFound.vue        # 404页
│   │
│   ├── router/
│   │   └── index.js            # 路由配置
│   │
│   ├── store/
│   │   └── index.js            # Pinia状态管理
│   │
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
│
└── README.md
```

---

## 仪表板布局设计

### 总体视图

```
┌─────────────────────────────────────────────────────────────────┐
│                     电商数据仓库分析系统                          │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│  [首页] [商品分析] [销售分析] [数据导出]                          │
└─────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────┐
│                          主仪表板                                 │
│                                                                  │
│ ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ ┌──────────┐ │
│ │ 销售总额    │  │ 订单总数    │  │ 平均订单价值 │ │ 退货率   │ │
│ │ ¥2.5M       │  │ 15K         │  │ ¥2,500      │ │ 5.2%     │ │
│ └─────────────┘  └─────────────┘  └─────────────┘ └──────────┘ │
│                                                                  │
│ ┌─────────────────────────┐  ┌─────────────────────────┐        │
│ │  热销商品 TOP 10        │  │  热评商品 TOP 10        │        │
│ │  (按销量)               │  │  (按评分)               │        │
│ │                         │  │                         │        │
│ │  1. iPhone 14 - 1500件  │  │  1. 南孚电池 - 4.9星    │        │
│ │  2. Mate 50 - 1200件    │  │  2. AirPods - 4.8星     │        │
│ │  3. 小米手环 - 950件    │  │  3.                     │        │
│ │  ...                    │  │  ...                    │        │
│ │ [柱状图总体趋势]        │  │ [柱状图总体趋势]        │        │
│ └─────────────────────────┘  └─────────────────────────┘        │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────┐   │
│ │  季节销售热力图 - 按类别统计                              │   │
│ │                   Spring Summer Fall Winter              │   │
│ │  Electronics      500K   620K   580K  720K              │   │
│ │  Accessories      50K    60K    70K   80K               │   │
│ │  Wearables        120K   140K   130K  150K              │   │
│ │  ...                                                    │   │
│ │  [热力图visualization]                                  │   │
│ └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│ ┌──────────────────┐  ┌──────────────────┐                     │
│ │ AOV日均值趋势    │  │ 退货率分布        │                     │
│ │ (过去30天)       │  │                   │                     │
│ │ [折线图]         │  │ [饼图]            │                     │
│ │ 2024-01进度...   │  │ Electronics 8%   │                     │
│ │                  │  │ Accessories 3%   │                     │
│ └──────────────────┘  └──────────────────┘                     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 核心Vue组件

### 1. 热销商品组件 (HotProductChart.vue)

```vue
<template>
  <div class="hot-product-container">
    <div class="header">
      <h2>热销商品分析</h2>
      <div class="tabs">
        <el-radio-group v-model="activeTab" @change="refreshData">
          <el-radio-button label="sales">按销量</el-radio-button>
          <el-radio-button label="reviews">按评分</el-radio-button>
          <el-radio-button label="combined">综合评分</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="content">
      <!-- 柱状图 -->
      <div class="chart-container">
        <div
          id="hotProductChart"
          ref="chartRef"
          style="width: 100%; height: 400px;"
        ></div>
      </div>

      <!-- 数据表格 -->
      <div class="table-container">
        <el-table :data="tableData" stripe>
          <el-table-column
            prop="rank"
            label="排名"
            width="80"
          ></el-table-column>
          <el-table-column
            prop="name"
            label="商品名称"
            width="200"
          ></el-table-column>
          <el-table-column
            prop="category"
            label="分类"
            width="100"
          ></el-table-column>
          <el-table-column
            prop="brand"
            label="品牌"
            width="100"
          ></el-table-column>
          <el-table-column
            prop="totalQty"
            label="销量"
            width="100"
          ></el-table-column>
          <el-table-column
            prop="totalAmount"
            label="销售额"
            width="120"
          ></el-table-column>
          <el-table-column
            prop="avgRating"
            label="评分"
            width="80"
          ></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import { getTopProducts } from "@/api/product";

const activeTab = ref("sales");
const chartRef = ref(null);
const tableData = ref([]);
let chart = null;

const refreshData = async () => {
  try {
    const response = await getTopProducts(activeTab.value, 10);
    tableData.value = response.data;

    // 更新图表
    updateChart(response.data);
  } catch (error) {
    ElMessage.error("数据加载失败");
  }
};

const updateChart = (data) => {
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  const option = {
    title: {
      text: `热销商品 - ${activeTab.value === "sales" ? "销量榜" : activeTab.value === "reviews" ? "评分榜" : "综合榜"}`,
    },
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
    },
    xAxis: {
      type: "category",
      data: data.map((item) => item.name),
      axisLabel: { rotate: 45, interval: 0 },
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        name: activeTab.value === "sales" ? "销量" : "评分",
        data: data.map((item) =>
          activeTab.value === "sales"
            ? item.totalQty
            : activeTab.value === "reviews"
              ? item.avgRating
              : item.totalQty,
        ),
        type: "bar",
        itemStyle: { color: "#409EFF" },
      },
    ],
  };

  chart.setOption(option);
};

onMounted(() => {
  refreshData();
});
</script>

<style scoped>
.hot-product-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 18px;
}

.chart-container {
  margin-bottom: 20px;
}

.table-container {
  width: 100%;
}
</style>
```

### 2. 季节销售图表 (SeasonSalesChart.vue)

```vue
<template>
  <div class="season-sales-container">
    <div class="header">
      <h2>季节销售分析</h2>
      <el-select v-model="selectedYear" @change="refreshData">
        <el-option label="2024年" value="2024"></el-option>
        <el-option label="2023年" value="2023"></el-option>
      </el-select>
    </div>

    <div class="chart-container">
      <div
        id="seasonChart"
        ref="chartRef"
        style="width: 100%; height: 500px;"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import * as echarts from "echarts";
import { getSeasonalSales } from "@/api/sales";

const selectedYear = ref("2024");
const chartRef = ref(null);
let chart = null;

const refreshData = async () => {
  try {
    const response = await getSeasonalSales(selectedYear.value);
    updateChart(response.data);
  } catch (error) {
    console.error("数据加载失败", error);
  }
};

const updateChart = (data) => {
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  const categories = [...new Set(data.map((item) => item.category))];
  const seasons = ["Spring", "Summer", "Fall", "Winter"];

  const series = seasons.map((season) => ({
    name: season,
    type: "bar",
    data: categories.map((cat) => {
      const item = data.find((d) => d.category === cat && d.season === season);
      return item ? item.totalAmount : 0;
    }),
  }));

  const option = {
    title: { text: `${selectedYear.value}年季节销售对比` },
    tooltip: { trigger: "axis" },
    legend: { data: seasons },
    xAxis: {
      type: "category",
      data: categories,
    },
    yAxis: { type: "value" },
    series: series,
  };

  chart.setOption(option);
};

onMounted(() => {
  refreshData();
});
</script>

<style scoped>
.season-sales-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 18px;
}
</style>
```

### 3. 平均订单价值组件 (AOVChart.vue)

```vue
<template>
  <div class="aov-container">
    <div class="header">
      <h2>平均订单价值趋势</h2>
      <el-select v-model="dimension" @change="refreshData">
        <el-option label="日" value="day"></el-option>
        <el-option label="周" value="week"></el-option>
        <el-option label="月" value="month"></el-option>
      </el-select>
    </div>

    <div class="chart-container">
      <div
        id="aovChart"
        ref="chartRef"
        style="width: 100%; height: 300px;"
      ></div>
    </div>

    <div class="stats">
      <div class="stat-item">
        <span class="label">最高AOV:</span>
        <span class="value">¥{{ maxAOV }}</span>
      </div>
      <div class="stat-item">
        <span class="label">最低AOV:</span>
        <span class="value">¥{{ minAOV }}</span>
      </div>
      <div class="stat-item">
        <span class="label">平均AOV:</span>
        <span class="value">¥{{ avgAOV }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import * as echarts from "echarts";
import { getAOVAnalysis } from "@/api/analytics";

const dimension = ref("day");
const chartRef = ref(null);
const aovData = ref([]);
let chart = null;

const maxAOV = computed(() => {
  return aovData.value.reduce((max, item) => Math.max(max, item.aov), 0);
});

const minAOV = computed(() => {
  return aovData.value.reduce((min, item) => Math.min(min, item.aov), Infinity);
});

const avgAOV = computed(() => {
  const sum = aovData.value.reduce((acc, item) => acc + item.aov, 0);
  return (sum / aovData.value.length).toFixed(2);
});

const refreshData = async () => {
  try {
    const response = await getAOVAnalysis(dimension.value);
    aovData.value = response.data;
    updateChart();
  } catch (error) {
    console.error("数据加载失败", error);
  }
};

const updateChart = () => {
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  const option = {
    tooltip: { trigger: "axis" },
    xAxis: {
      type: "category",
      data: aovData.value.map((item) => item.date),
      axisLabel: { rotate: 45 },
    },
    yAxis: { type: "value" },
    series: [
      {
        name: "AOV",
        data: aovData.value.map((item) => item.aov),
        type: "line",
        smooth: true,
        itemStyle: { color: "#67C23A" },
      },
    ],
  };

  chart.setOption(option);
};

onMounted(() => {
  refreshData();
});
</script>

<style scoped>
.aov-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-container {
  margin-bottom: 20px;
}

.stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  gap: 10px;
}

.label {
  color: #666;
}

.value {
  font-weight: bold;
  color: #409eff;
  font-size: 18px;
}
</style>
```

---

## API 调用模块 (axios配置)

### api/product.js

```javascript
import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

export const getTopProducts = (type, limit = 10) => {
  const apiMap = {
    sales: "/products/top-sales",
    reviews: "/products/top-reviews",
    combined: "/products/combined-top",
  };

  return API.get(apiMap[type], { params: { limit } });
};

export default API;
```

---

## 部署配置 (package.json)

```json
{
  "name": "ecommerce-warehouse-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.3.0",
    "element-plus": "^2.3.0",
    "echarts": "^5.4.0",
    "axios": "^1.3.0",
    "pinia": "^2.0.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.0.0",
    "vite": "^4.2.0"
  }
}
```

---

## Docker 部署 (Dockerfile)

```dockerfile
# Build阶段
FROM node:16 as build
WORKDIR /app
COPY package.json yarn.lock ./
RUN yarn install
COPY . .
RUN yarn build

# 运行阶段
FROM nginx:latest
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 可视化功能列表

- ✅ 热销商品排行（销量/评分/综合）
- ✅ 季节销售热力图
- ✅ 平均订单价值趋势
- ✅ 商品退货率分布
- ✅ 日KPI统计
- ✅ 品类销售对比
- ✅ 用户分析（可选）
- ✅ 数据导出功能（Excel）
