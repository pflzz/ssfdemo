package com.pfl.ssfmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession // 整合 redis 作为 session 存储
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SsfmallAuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsfmallAuthServerApplication.class, args);
    }
}
