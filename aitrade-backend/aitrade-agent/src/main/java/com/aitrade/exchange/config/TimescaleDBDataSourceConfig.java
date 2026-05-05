package com.aitrade.exchange.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * TimescaleDB（PostgreSQL）数据源配置
 */
@Configuration
public class TimescaleDBDataSourceConfig {

    @Bean(name = "timescaleDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.timescaledb")
    public DataSource timescaleDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "timescaleJdbcTemplate")
    public JdbcTemplate timescaleJdbcTemplate(@Qualifier("timescaleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

