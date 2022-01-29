package com.sl.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/13 17:15
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        //实例化bean
        RedisTemplate<String,Object> template=new RedisTemplate<>();
        //将工厂设置给template（具有了访问数据库的能力）
        template.setConnectionFactory(factory);
        //序列化的方式（数据转换的方式）
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());

        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());

        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());

        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //将设置生效
        template.afterPropertiesSet();

        return template;

    }
}
