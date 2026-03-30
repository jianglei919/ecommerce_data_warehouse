# Java Demo 项目 - 后端架构设计

## 项目结构

```
ecommerce-warehouse-java/
├── pom.xml                                    # Maven配置
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── config/
│   │   │   │   ├── DataSourceConfig.java      # 多数据源配置
│   │   │   │   └── MyBatisPlusConfig.java     # MyBatis Plus配置
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── ProductController.java     # 商品分析API
│   │   │   │   ├── SalesController.java       # 销售分析API
│   │   │   │   └── AnalyticsController.java   # 综合分析API
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java        # 商品业务逻辑
│   │   │   │   ├── SalesService.java          # 销售业务逻辑
│   │   │   │   ├── AnalyticsService.java      # 分析业务逻辑
│   │   │   │   └── impl/                      # 接口实现
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   ├── ProductMapper.java
│   │   │   │   ├── OrderMapper.java
│   │   │   │   ├── ReviewMapper.java
│   │   │   │   └── AnalysisMapper.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── Product.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Review.java
│   │   │   │   └── Return.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── HotProductDTO.java         # 热销商品DTO
│   │   │   │   ├── ReturnRateDTO.java         # 退货率DTO
│   │   │   │   ├── SeasonSalesDTO.java        # 季节销售DTO
│   │   │   │   └── AOVAnalysisDTO.java        # AOV分析DTO
│   │   │   │
│   │   │   └── Application.java               # 启动类
│   │   │
│   │   └── resources/
│   │       ├── application.yml                # 配置文件
│   │       └── mapper/                        # MyBatis XML文件
│   │           ├── ProductMapper.xml
│   │           ├── SalesMapper.xml
│   │           └── AnalysisMapper.xml
│   │
│   └── test/                                  # 测试类
│       └── java/
│
├── sql/
│   ├── source_db.sql                          # 业务库建表脚本
│   ├── warehouse_db.sql                       # 分析库建表脚本
│   └── sample_data.sql                        # 样本数据
│
└── docs/
    ├── API文档.md
    ├── 部署说明.md
    └── 开发指南.md
```

---

## pom.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>ecommerce-warehouse-demo</artifactId>
    <version>1.0.0</version>
    <name>E-Commerce Data Warehouse Demo</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <spring-boot.version>2.7.0</spring-boot.version>
        <mybatis-plus.version>3.5.1</mybatis-plus.version>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
    </parent>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- JSON处理 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.83</version>
        </dependency>

        <!-- Swagger文档 -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.9.2</version>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.9.2</version>
        </dependency>

        <!-- DynamicDataSource -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
            <version>3.5.0</version>
        </dependency>

        <!-- Redis（可选） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 核心代码示例

### 1. 启动类 (Application.java)

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 多数据源配置 (DataSourceConfig.java)

```java
package com.example.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 * 配置两个数据库：
 * 1. primary - 业务原始数据库 (ecommerce_source)
 * 2. warehouse - 分析数据库 (ecommerce_warehouse)
 */
@Configuration
public class DataSourceConfig {

    /**
     * 业务数据库数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 分析数据库数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.warehouse")
    public DataSource warehouseDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 动态数据源（默认为primary）
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();

        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("primary", primaryDataSource());
        dataSourceMap.put("warehouse", warehouseDataSource());

        dynamicRoutingDataSource.setDataSources(dataSourceMap);
        dynamicRoutingDataSource.setDefaultDataSource(primaryDataSource());

        return dynamicRoutingDataSource;
    }
}
```

### 3. 实体类示例 (Product.java)

```java
package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer productId;

    private String name;

    private String description;

    private String category;

    private BigDecimal price;

    private BigDecimal cost;

    private String brand;

    private Integer stockQty;

    private Boolean isActive;

    private Date createdAt;

    private Date updatedAt;
}
```

### 4. DTO示例 (HotProductDTO.java)

```java
package com.example.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * 热销商品信息DTO
 */
@Data
@AllArgsConstructor
public class HotProductDTO {

    private Integer productId;

    private String name;

    private String category;

    private String brand;

    private Integer totalQty;  // 总销量

    private BigDecimal totalAmount;  // 总销售额

    private Integer orderCount;  // 订单数

    private BigDecimal avgRating;  // 平均评分

    private Integer reviewCount;  // 评论数

    private Integer rank;  // 排名

    private String rankType;  // 排名类型：sales/reviews/combined
}
```

### 5. Mapper接口示例 (ProductMapper.java)

```java
package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dto.HotProductDTO;
import com.example.entity.Product;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 获取热销商品TOP N（按销量）
     */
    @Select("SELECT " +
            "  p.product_id, p.name, p.category, p.brand, " +
            "  SUM(oi.quantity) as total_qty, " +
            "  SUM(oi.line_total) as total_amount, " +
            "  COUNT(DISTINCT o.order_id) as order_count, " +
            "  ROUND(AVG(pr.rating), 2) as avg_rating, " +
            "  COUNT(pr.review_id) as review_count, " +
            "  RANK() OVER (ORDER BY SUM(oi.quantity) DESC) as rank " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "LEFT JOIN product_reviews pr ON p.product_id = pr.product_id " +
            "WHERE o.status = 'completed' " +
            "GROUP BY p.product_id, p.name, p.category, p.brand " +
            "ORDER BY total_qty DESC " +
            "LIMIT #{limit}")
    List<HotProductDTO> selectTopProductsBySales(Integer limit);

    /**
     * 获取热销商品TOP N（按评分）
     */
    @Select("SELECT " +
            "  p.product_id, p.name, p.category, p.brand, " +
            "  SUM(oi.quantity) as total_qty, " +
            "  SUM(oi.line_total) as total_amount, " +
            "  COUNT(DISTINCT o.order_id) as order_count, " +
            "  ROUND(AVG(pr.rating), 2) as avg_rating, " +
            "  COUNT(pr.review_id) as review_count, " +
            "  RANK() OVER (ORDER BY AVG(pr.rating) DESC) as rank " +
            "FROM products p " +
            "LEFT JOIN product_reviews pr ON p.product_id = pr.product_id " +
            "LEFT JOIN order_items oi ON p.product_id = oi.product_id " +
            "LEFT JOIN orders o ON oi.order_id = o.order_id " +
            "GROUP BY p.product_id, p.name, p.category, p.brand " +
            "HAVING COUNT(pr.review_id) >= 5 " +
            "ORDER BY avg_rating DESC, review_count DESC " +
            "LIMIT #{limit}")
    List<HotProductDTO> selectTopProductsByReviews(Integer limit);
}
```

### 6. Service接口和实现 (ProductService.java)

```java
package com.example.service;

import com.example.dto.HotProductDTO;
import java.util.List;

public interface ProductService {

    /**
     * 获取热销商品（按销量）
     */
    List<HotProductDTO> getTopProductsBySales(Integer limit);

    /**
     * 获取热销商品（按评分）
     */
    List<HotProductDTO> getTopProductsByReviews(Integer limit);

    /**
     * 获取热销商品（综合评分）
     */
    List<HotProductDTO> getTopProductsByCombinedScore(Integer limit);
}
```

```java
package com.example.service.impl;

import com.example.dto.HotProductDTO;
import com.example.mapper.ProductMapper;
import com.example.service.ProductService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public List<HotProductDTO> getTopProductsBySales(Integer limit) {
        return productMapper.selectTopProductsBySales(limit != null ? limit : 10);
    }

    @Override
    public List<HotProductDTO> getTopProductsByReviews(Integer limit) {
        return productMapper.selectTopProductsByReviews(limit != null ? limit : 10);
    }

    @Override
    public List<HotProductDTO> getTopProductsByCombinedScore(Integer limit) {
        // 综合评分 = 销量排名权重(60%) + 评分排名权重(40%)
        List<HotProductDTO> salesTop = getTopProductsBySales(20);
        List<HotProductDTO> reviewTop = getTopProductsByReviews(20);

        // 合并和计算综合分数...
        // 实现细节省略

        return salesTop;
    }
}
```

### 7. Controller示例 (ProductController.java)

```java
package com.example.controller;

import com.example.dto.HotProductDTO;
import com.example.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Api(tags = "商品分析")
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping("/top-sales")
    @ApiOperation("热销商品（按销量）")
    public Map<String, Object> getTopSales(@RequestParam(defaultValue = "10") Integer limit) {
        List<HotProductDTO> data = productService.getTopProductsBySales(limit);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    @GetMapping("/top-reviews")
    @ApiOperation("热销商品（按评分）")
    public Map<String, Object> getTopReviews(@RequestParam(defaultValue = "10") Integer limit) {
        List<HotProductDTO> data = productService.getTopProductsByReviews(limit);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    @GetMapping("/combined-top")
    @ApiOperation("热销商品（综合评分）")
    public Map<String, Object> getCombinedTop(@RequestParam(defaultValue = "10") Integer limit) {
        List<HotProductDTO> data = productService.getTopProductsByCombinedScore(limit);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }
}
```

---

## application.yml 配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: ecommerce-warehouse-demo

  # 主库（业务数据库）
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/ecommerce_source?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver

    # 从库（分析库）
    warehouse:
      url: jdbc:mysql://localhost:3306/ecommerce_warehouse?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    root: INFO
    com.example: DEBUG
```

---

## API 端点概览

| 端点                         | 方法 | 说明                       |
| ---------------------------- | ---- | -------------------------- |
| `/api/products/top-sales`    | GET  | 热销商品（按销量TOP 10）   |
| `/api/products/top-reviews`  | GET  | 热销商品（按评分TOP 10）   |
| `/api/products/combined-top` | GET  | 热销商品（综合评分TOP 10） |
| `/api/sales/by-season`       | GET  | 按季节统计各类别销量       |
| `/api/analytics/aov`         | GET  | 平均订单价值分析           |
| `/api/analytics/return-rate` | GET  | 商品退货率分析             |
| `/api/analytics/daily-kpi`   | GET  | 每日KPI统计                |
