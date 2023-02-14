package com.pfl.ssfmall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cart.thread.pool")
@Component
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize = 20;
    private Integer maxSize = 200;
    private Integer keepAliveTime = 10;
}
