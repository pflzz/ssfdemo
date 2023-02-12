package com.pfl.ssfmall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.pfl.ssfmall.member.feign")
public class SsfmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsfmallMemberApplication.class, args);
    }

}
