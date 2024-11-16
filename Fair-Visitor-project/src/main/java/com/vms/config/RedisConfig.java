package com.vms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.vms.dto.AllPendingVisitsDTO;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, AllPendingVisitsDTO> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, AllPendingVisitsDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<AllPendingVisitsDTO>(AllPendingVisitsDTO.class));
        return template;
    }
}
