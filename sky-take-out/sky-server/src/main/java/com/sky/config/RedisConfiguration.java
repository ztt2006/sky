package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();

        //     设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // key序列化
        redisTemplate.setKeySerializer(stringSerializer);
        // // hash的key序列化
        // redisTemplate.setHashKeySerializer(stringSerializer);
        // // value序列化（重点！你之前缺了这行）
        // redisTemplate.setValueSerializer(stringSerializer);
        // // hash的value序列化
        // redisTemplate.setHashValueSerializer(stringSerializer);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        // 必须调用，初始化属性
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
