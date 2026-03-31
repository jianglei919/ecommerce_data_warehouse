package com.uwindsor.warehouse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 多数据源配置 - 支持 App、Web、Warehouse 三个数据库
 */
@Configuration
public class DataSourceConfig {

    /**
     * App 数据源配置
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.app")
    public HikariConfig appHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);
        return config;
    }

    @Bean
    public DataSource appDataSource() {
        return new HikariDataSource(appHikariConfig());
    }

    /**
     * Web 数据源配置
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.web")
    public HikariConfig webHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);
        return config;
    }

    @Bean
    public DataSource webDataSource() {
        return new HikariDataSource(webHikariConfig());
    }

    /**
     * Warehouse 数据源配置 (主数据源)
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.warehouse")
    public HikariConfig warehouseHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);
        return config;
    }

    @Bean
    @Primary
    public DataSource warehouseDataSource() {
        return new HikariDataSource(warehouseHikariConfig());
    }

    /**
     * Warehouse SqlSessionFactory (主工厂)
     */
    @Bean
    @Primary
    public SqlSessionFactory warehouseSqlSessionFactory(@Qualifier("warehouseDataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/warehouse/*.xml"));
        return bean.getObject();
    }

    /**
     * App SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory appSqlSessionFactory(@Qualifier("appDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/app/*.xml"));
        return bean.getObject();
    }

    /**
     * Web SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory webSqlSessionFactory(@Qualifier("webDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/web/*.xml"));
        return bean.getObject();
    }

}
