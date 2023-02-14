package com.pfl.ssfmall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableRabbit
@EnableFeignClients(basePackages = "com.pfl.ssfmall.order.feign")
@SpringBootApplication
public class SsfmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsfmallOrderApplication.class, args);
    }

}
