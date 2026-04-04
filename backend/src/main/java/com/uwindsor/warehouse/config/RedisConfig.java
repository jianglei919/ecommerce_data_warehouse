package com.uwindsor.warehouse.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 连接配置
 * 显式配置 Lettuce 连接工厂，覆盖 Spring Boot 自动配置中的 localhost 硬编码问题
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:redis}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.timeout:2000ms}")
    private Duration timeout;

    @Value("${spring.redis.database:0}")
    private int database;

    /**
     * Lettuce 连接工厂配置
     * 在 Docker 环境中，必须显式指定 redis 主机名而不依赖自动配置
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("🔧 Configuring Redis connection: {}:{}", redisHost, redisPort);

        // 创建 Redis 连接配置
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(redisHost);
        standaloneConfig.setPort(redisPort);
        standaloneConfig.setDatabase(database);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            standaloneConfig.setPassword(redisPassword);
        }

        // 创建 Lettuce 客户端配置
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
                .commandTimeout(timeout)
                .shutdownTimeout(Duration.ofSeconds(2));

        // 设置 Lettuce 客户端选项
        ClientOptions clientOptions = ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP2) // Redis 协议版本
                .build();

        builder.clientOptions(clientOptions);

        // 创建连接工厂
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                standaloneConfig,
                builder.build());

        log.info("✅ Redis connection factory configured successfully");
        return factory;
    }

    /**
     * Redis 模板 Bean
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("🔧 Configuring Redis template");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 Jackson 序列化器
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Key 序列化器
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Value 序列化器
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        log.info("✅ Redis template configured successfully");
        return template;
    }
}
