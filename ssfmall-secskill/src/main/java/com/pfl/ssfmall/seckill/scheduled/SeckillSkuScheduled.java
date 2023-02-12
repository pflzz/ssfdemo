package com.pfl.ssfmall.seckill.scheduled;

import com.pfl.ssfmall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品定时上架
 */

@Service
@Slf4j
public class SeckillSkuScheduled {

    @Resource
    SeckillService seckillService;
    @Resource
    RedissonClient redissonClient;

    public static final String UPLOAD_LOCK = "seckill:upload:lock";

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 添加分布式锁，增加秒杀商品上架的幂等性处理
        // 锁的业务流程执行完成，状态已经更新完成，释放锁以后，其他人获取到的是最新的业务状态
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        // 上锁
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }

}
