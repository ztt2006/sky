package com.sky.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.sky.json.JacksonObjectMapper;
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

        // Use the application's Jackson configuration so Java time values (for example,
        // DishVO.updateTime) are serialized with the same module and format as HTTP JSON.
        // GenericJackson2JsonRedisSerializer needs default typing to reconstruct the
        // concrete element types when a value is read back from Redis.
        ObjectMapper objectMapper = new JacksonObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        // 必须调用，初始化属性
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
