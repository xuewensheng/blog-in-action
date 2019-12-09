package com.spring.boot.blog.initializrstart.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig  {

    @Value("${spring.redis.cluster.nodes}")
    private String nodes;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.lettuce.pool.maxIdle:}")
    private Integer maxIdle;
    @Value("${spring.redis.lettuce.pool.minIdle:}")
    private Integer minIdle;
    @Value("${spring.redis.lettuce.pool.maxTotal:}")
    private Integer maxTotal;
    @Value("${spring.redis.lettuce.pool.maxWaitMillis:}")
    private Long maxWaitMillis;
    @Value("${spring.redis.lettuce.pool.maxActive:}")
    private Long maxActive;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        LettucePoolingClientConfiguration lettucePoolingClientConfiguration = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .build();
        //单机redis
        RedisStandaloneConfiguration redisConfig  = new RedisStandaloneConfiguration();
        redisConfig.setHostName(StringUtils.isBlank(host) ? "localhost":host.split(":")[0]);
        redisConfig.setPort(port != null ? 6379: Integer.valueOf(host.split(":")[1]));
        redisConfig.setPassword(StringUtils.isNotBlank(password) ? password : "");
        // 集群redis
        /*RedisClusterConfiguration redisConfig = new RedisClusterConfiguration();
        Set<RedisNode> nodeses = new HashSet<>();*/
        return new LettuceConnectionFactory(redisConfig, lettucePoolingClientConfiguration);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(lettuceConnectionFactory);

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();

        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式

        template.setHashKeySerializer(stringRedisSerializer);

        // value序列化方式采用jackson

        template.setValueSerializer(jackson2JsonRedisSerializer);

        // hash的value序列化方式采用jackson

        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        return template;
    }
}
