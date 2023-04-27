package com.sre.digital.unittesting.config;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import com.sre.digital.unittesting.model.Item;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;

@Configuration
@EnableCaching
@ComponentScan
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {

        return Redisson.create();
    }

    public RedisConnectionFactory redisConnectionFactory() {

        return new RedissonConnectionFactory(redissonClient());

    }

    @Bean
    public RedisTemplate<String, Item> redisTemplate() {
        RedisTemplate<String, Item> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Item.class));
        return template;
    }
}
