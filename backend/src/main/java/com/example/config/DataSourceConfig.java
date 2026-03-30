package com.example.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 多数据源配置
 * 配置三个数据库：
 * 1. primary - 业务源库1 (ecommerce_source_1)
 * 2. warehouse - 业务源库2 (ecommerce_source_2)
 * 3. warehouse2 - 数据仓库 (ecommerce_warehouse)
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
     * 数据仓库数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.warehouse2")
    public DataSource warehouse2DataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 动态数据源（默认为primary）
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        dynamicRoutingDataSource.addDataSource("primary", primaryDataSource());
        dynamicRoutingDataSource.addDataSource("warehouse", warehouseDataSource());
        dynamicRoutingDataSource.addDataSource("warehouse2", warehouse2DataSource());
        dynamicRoutingDataSource.setPrimary("primary");

        return dynamicRoutingDataSource;
    }
}
