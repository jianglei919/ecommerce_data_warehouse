package com.example.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
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
