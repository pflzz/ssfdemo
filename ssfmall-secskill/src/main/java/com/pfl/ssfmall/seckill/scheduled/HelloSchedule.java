package com.pfl.ssfmall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@EnableScheduling // 开启定时任务
@Slf4j
@EnableAsync // 开启异步任务
@Component
public class HelloSchedule {

    @Scheduled(cron = "* * * ? * 5") // 秒时分 日月周
    @Async
    public void hello() throws InterruptedException {
        log.info("hello uzi");
        Thread.sleep(3000);
    }
}
