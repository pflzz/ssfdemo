package com.pfl.ssfmall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * redisson 分布式锁配置
 */
@Configuration
public class MyRedissonConfig {
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://43.138.81.213:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
