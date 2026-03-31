# E-Commerce Data Warehouse - Business Requirements Document

## Project Objective

Build a data warehouse system that extracts data from two different business data sources (App and Web), performs data cleaning and ETL processing, and finally displays **sales analysis** and **product ratings ranking** data.

---

## Data Source Architecture

### Database 1: App Business System (ecommerce_source_app)

**Table Structure:**

| Table Name          | Fields                                              | Description          |
| ------------------- | --------------------------------------------------- | -------------------- |
| **users**           | user_id, name, email, city, register_date           | User table           |
| **products**        | product_id, name, category, price, brand            | Product table        |
| **orders**          | order_id, user_id, order_date, total_amount, status | Order table          |
| **order_items**     | item_id, order_id, product_id, quantity, unit_price | Order details table  |
| **product_reviews** | review_id, product_id, user_id, rating, review_date | Product review table |

**Data Format Characteristics:**

- orders.**order_id**: Numeric type (INT)
- orders.**order_date**: Date format `yyyy-MM-dd`

### Database 2: Web Business System (ecommerce_source_web)

**Table Structure:** Same as App, but with different order table field names

| Table Name          | Fields                                              | Description                            |
| ------------------- | --------------------------------------------------- | -------------------------------------- |
| **users**           | user_id, name, email, city, register_date           | User table                             |
| **products**        | product_id, name, category, price, brand            | Product table                          |
| **orders**          | order_no, user_id, order_date, total_amount, status | Order table (Note: field name differs) |
| **order_items**     | item_id, order_no, product_id, quantity, unit_price | Order details(Note: uses order_no)     |
| **product_reviews** | review_id, product_id, user_id, rating, review_date | Product review table                   |

**Data Format Characteristics:**

- orders.**order_no**: Alphanumeric mixed (VARCHAR, e.g., "WEB-001")
- orders.**order_date**: Date format `MM/dd/yyyy`

**Key Differences Between Source Systems:**

| Dimension           | App (source_app) | Web (source_web)  |
| ------------------- | ---------------- | ----------------- |
| Order ID Field Name | order_id         | order_no          |
| Order ID Data Type  | 12345 (INT)      | WEB-001 (VARCHAR) |
| Order Date Format   | 2024-03-01       | 03/01/2024        |

### Database 3: Analytics Data Warehouse (ecommerce_warehouse)

Stores cleaned and transformed statistical data.

**Core Tables:**

| Table Name                      | Purpose                                        |
| ------------------------------- | ---------------------------------------------- |
| **fact_sales_by_category_time** | Sales quantity statistics by category and time |
| **fact_top_rated_products**     | Top products by rating statistics              |

---

## Requirements List

### Requirement 1: Analyze Sales by Product Category and Time Dimension

**Data Source**: `ecommerce_warehouse.fact_sales_by_category_time` (Warehouse table)

**Dimensions:**

- Product category (category)
- Time dimension: year, month, day

**Metrics:**

- Sales quantity (total_quantity)
- Sales amount (total_sales_amount)

**Output Display:**

- Heatmap (X-axis: time, Y-axis: category, value: quantity)
- Bar chart (comparison by category or time period)

**Example Query:**

```sql
-- Query must be based on warehouse table
SELECT
    category,
    CONCAT(year, '-', LPAD(month, 2, '0')) as time_period,
    total_quantity,
    total_sales_amount
FROM ecommerce_warehouse.fact_sales_by_category_time
WHERE year = 2024
ORDER BY category, year, month, day;
```

**Example Result:**

```
Category: Electronics, Time: 2024-03, Quantity: 150, Amount: 45000
Category: Clothing,    Time: 2024-03, Quantity: 200, Amount: 15000
Category: Books,       Time: 2024-03, Quantity: 80,  Amount: 3200
```

---

### Requirement 2: Top 5 Products by Review Rating

**Data Source**: `ecommerce_warehouse.fact_top_rated_products` (Warehouse table)

**Dimensions:**

- Product category (category)
- Time dimension: year, month, day

**Metrics:**

- Average rating (avg_rating)
- Review count (review_count)

**Output Display:**

- Ranking leaderboard (displaying Top 5 products with their ratings)

**Example Query:**

```sql
-- Query must be based on warehouse table
SELECT
    product_name,
    category,
    avg_rating,
    review_count
FROM ecommerce_warehouse.fact_top_rated_products
WHERE year = 2024 AND month = 3
ORDER BY avg_rating DESC, review_count DESC
LIMIT 5;
```

**Example Result:**

```
Product: iPhone 14,       Category: Electronics, Avg Rating: 4.8, Reviews: 150
Product: MacBook Pro,     Category: Electronics, Avg Rating: 4.7, Reviews: 120
Product: Samsung Galaxy,  Category: Electronics, Avg Rating: 4.6, Reviews: 100
...
```

---

## Data Processing Flow - ETL Architecture

```mermaid
graph LR
    subgraph source["Source"]
        app["APP<br/>INT"]
        web["WEB<br/>VARCHAR"]
    end

    subgraph etl["ETL"]
        extract["Extract"]
        transform["Transform"]
        clean["Validate"]
    end

    subgraph warehouse["Warehouse"]
        sales["Fact Sales"]
        top["Top Products"]
    end

    subgraph analytics["Analytics"]
        query1["Query 1"]
        query2["Query 2"]
    end

    subgraph ui["UI"]
        chart1["Charts"]
        chart2["Board"]
    end

    app --> extract
    web --> extract
    extract --> transform
    transform --> clean
    clean --> sales
    clean --> top

    sales --> query1
    top --> query2

    query1 --> chart1
    query2 --> chart2

    style source fill:#e3f2fd,stroke:#1976d2,stroke-width:2px,color:#000
    style etl fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#000
    style warehouse fill:#fff3e0,stroke:#f57c00,stroke-width:2px,color:#000
    style analytics fill:#e8f5e9,stroke:#388e3c,stroke-width:2px,color:#000
    style ui fill:#fce4ec,stroke:#c2185b,stroke-width:2px,color:#000
```

**Process Description:**

| Layer                  | Components                  | Color     | Details                             |
| ---------------------- | --------------------------- | --------- | ----------------------------------- |
| 📊 **Source Layer**    | App / Web Sources           | 🔵 Blue   | Two heterogeneous business systems  |
| ⚙️ **ETL Layer**       | Extract → Transform → Clean | 🟣 Purple | Data cleaning and format conversion |
| 📈 **Warehouse Layer** | Two core fact tables        | 🟠 Orange | **Data source for all queries**     |
| 📊 **Analytics Layer** | Warehouse-based queries     | 🟢 Green  | **Must query warehouse tables**     |
| 🖥️ **UI Layer**        | Charts and dashboards       | 🔴 Pink   | Final user interface                |

---

## Phase V2 - Requirements Update: Unified Orders Table

### V2 Requirements Overview

As the project evolves, the following new requirements have been added to the existing roadmap:

1. **Unified Order Data Layer** - Create a unified orders table in the warehouse that consolidates order data from both App and Web business systems
2. **Data Aggregation Foundation** - Existing Fact tables and subsequent analyses should be built on the new unified orders table
3. **Management Dashboard UI** - Create a frontend interface to display integrated order data and statistics

### V2 Data Structure Design

#### Heterogeneous Source System Challenge

Order identifier differences between the two business systems:

| Dimension               | App System  | Web System             |
| ----------------------- | ----------- | ---------------------- |
| **Order ID Field Name** | order_id    | order_no               |
| **Order ID Data Type**  | INT (12345) | VARCHAR (WEB-2024-001) |
| **Date Format**         | yyyy-MM-dd  | MM/dd/yyyy             |

#### Unified Orders Table Design

**Table 1: unified_orders** (Unified Orders Master Table)

```sql
CREATE TABLE unified_orders (
    unified_order_id INT PRIMARY KEY AUTO_INCREMENT,
    source ENUM('APP', 'WEB') NOT NULL,        -- Data source identifier
    app_order_id INT NULLABLE,                 -- App system Order ID
    web_order_no VARCHAR(50) NULLABLE,         -- Web system Order Number
    user_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_source_order (source, app_order_id, web_order_no),
    KEY idx_source (source),
    KEY idx_order_date (order_date),
    KEY idx_user_id (user_id)
);
```

**Table 2: unified_order_items** (Unified Order Details Table)

```sql
CREATE TABLE unified_order_items (
    unified_item_id INT PRIMARY KEY AUTO_INCREMENT,
    unified_order_id INT NOT NULL,             -- FK to unified_orders
    product_id INT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (unified_order_id) REFERENCES unified_orders (unified_order_id),
    KEY idx_unified_order_id (unified_order_id),
    KEY idx_product_id (product_id)
);
```

**Design Highlights:**

- Composite unique constraint `(source, app_order_id, web_order_no)` ensures order uniqueness and traceability
- source field identifies data origin for cross-source analysis
- Two nullable ID fields serve App and Web data respectively
- Details table contains necessary attributes for aggregation queries

#### Sample Data

```sql
-- App source orders (6 records)
INSERT INTO unified_orders (source, app_order_id, user_id, order_date, total_amount, status) VALUES
('APP', 1001, 1, '2024-01-15', 999.99, 'completed'),
('APP', 1002, 2, '2024-01-16', 599.99, 'completed'),
('APP', 1003, 3, '2024-01-17', 1899.97, 'completed');

-- Web source orders (10 records)
INSERT INTO unified_orders (source, web_order_no, user_id, order_date, total_amount, status) VALUES
('WEB', 'WEB-2024-001', 1, '2024-01-15', 899.99, 'completed'),
('WEB', 'WEB-2024-002', 2, '2024-01-16', 229.99, 'completed'),
('WEB', 'WEB-2024-003', 3, '2024-01-17', 1999.97, 'completed');

-- Summary: Total 16 unified orders with 27 order items
```

### V2 API Interface Design

#### Backend REST API Endpoints

| Endpoint                                  | Method | Function                        | Query Parameters               |
| ----------------------------------------- | ------ | ------------------------------- | ------------------------------ |
| `/api/unified-orders`                     | GET    | Fetch orders list (paginated)   | page, pageSize, source, status |
| `/api/unified-orders/{id}`                | GET    | Get order details with items    | -                              |
| `/api/unified-orders/overview`            | GET    | Dashboard overview statistics   | -                              |
| `/api/unified-orders/stats/by-source`     | GET    | Statistics by source (APP/WEB)  | -                              |
| `/api/unified-orders/stats/product-sales` | GET    | Product sales analysis          | -                              |
| `/api/unified-orders/by-source/{source}`  | GET    | Query orders by specific source | page, pageSize                 |

**Response Example:**

```json
{
  "status": "success",
  "data": [
    {
      "unifiedOrderId": 1,
      "source": "APP",
      "appOrderId": 1001,
      "webOrderNo": null,
      "userId": 1,
      "orderDate": "2024-01-15",
      "totalAmount": 999.99,
      "status": "completed"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "total": 16,
    "totalPages": 1
  },
  "timestamp": "2026-03-31T01:24:34"
}
```

### V2 Frontend Dashboard Design

#### Unified Orders Dashboard (UnifiedOrders.vue)

**Feature Modules:**

1. **Overview Statistics Cards** - Display key metrics
   - Total Orders: 16
   - APP Orders: 6 (Amount: $8,699.86)
   - WEB Orders: 10 (Amount: $9,589.34)

2. **Filters** - Support multi-dimensional filtering
   - Source filter (APP/WEB)
   - Order status filter (pending/completed/cancelled)
   - Search and sort

3. **Order List** - Paginated table view
   - Unified Order ID, Source, Order No., Date, Amount, Status
   - Color coding: APP (blue), WEB (green)
   - 20 records per page

4. **Order Detail Modal** - Display order details
   - Basic order information
   - Associated order items (product, quantity, price)
   - Order statistics

5. **Analytics Dashboard** - Aggregated data view
   - Sales statistics by source
   - Top selling products

### V2 Implementation Progress

| Phase          | Task                                   | Status      | Completion Date |
| -------------- | -------------------------------------- | ----------- | --------------- |
| **Design**     | Unified orders table design            | ✅ Complete | 2026-03-30      |
| **Database**   | Create table structures and indexes    | ✅ Complete | 2026-03-30      |
| **Database**   | Insert sample data                     | ✅ Complete | 2026-03-30      |
| **Backend**    | Implement domain models                | ✅ Complete | 2026-03-30      |
| **Backend**    | Implement data access layer            | ✅ Complete | 2026-03-30      |
| **Backend**    | Implement API controller (6 endpoints) | ✅ Complete | 2026-03-30      |
| **Frontend**   | Create Vue3 component                  | ✅ Complete | 2026-03-30      |
| **Frontend**   | Integrate routing and navigation       | ✅ Complete | 2026-03-30      |
| **Deployment** | Rebuild Docker images                  | ✅ Complete | 2026-03-30      |
| **Testing**    | API functionality testing              | ✅ Complete | 2026-03-30      |
| **Testing**    | Frontend UI verification               | ✅ Complete | 2026-03-30      |

### V2 Expected Benefits

1. **Data Unification** - Eliminate App/Web system key type differences and provide a unified data view
2. **Query Optimization** - Unified table structure enables more standardized Fact table construction
3. **User Experience** - Administrators can monitor cross-source order data via the unified dashboard
4. **Scalability** - Establishes foundation for future multi-source data integration

---

## Key Principles

### All Analytics Queries Must Use the Data Warehouse

**Key Points:**

- Forbidden to directly query `ecommerce_source_app` or `ecommerce_source_web`
- Required to query from the two fact tables in `ecommerce_warehouse`
- All data must go through ETL processing, format unification, and quality validation before use
- Warehouse tables automatically handle App/Web data format differences

**Reasons:**

1. **Data Consistency** - Ensures data formats are unified across both channels
2. **Data Quality** - Data is cleaned, deduplicated, and validated
3. **Performance Optimization** - Warehouse tables are index-optimized for query efficiency
4. **Unified Business Logic** - All analyses are based on the same data processing rules

---

## Technical Requirements

- **Backend**: Spring Boot + MyBatis, supporting multi-source queries and data transformation
- **Frontend**: Vue 3, using charting libraries for heatmaps, bar charts, and leaderboards
- **Database**: MySQL 8.0
- **Deployment**: Docker Compose one-click startup

---

## Workflow

1. Requirements Confirmation (Current stage) - Confirm data model and statistical requirements
2. Database DDL Development - Create tables in source and warehouse databases
3. Sample Data Insertion - Insert test data into source systems (for demonstration)
4. ETL Logic Development - Data cleaning, transformation, and warehouse loading
5. API Development - Backend query endpoints
6. UI Development - Frontend dashboard
7. Testing and Deployment - Docker deployment verification
