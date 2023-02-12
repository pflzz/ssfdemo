package com.pfl.ssfmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.seckill.feign.CouponFeignService;
import com.pfl.ssfmall.seckill.feign.ProductFeignService;
import com.pfl.ssfmall.seckill.service.SeckillService;
import com.pfl.ssfmall.seckill.to.SeckillSkuTo;
import com.pfl.ssfmall.seckill.vo.SeckillSessionWithSkus;
import com.pfl.ssfmall.seckill.vo.SeckillSkuVo;
import com.pfl.ssfmall.seckill.vo.SkuInfo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Resource
    CouponFeignService couponFeignService;
    @Resource
    ProductFeignService productFeignService;
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    RedissonClient redissonClient;

    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    public static final String SESSIONS_SKUS_CACHE_PREFIX = "seckill:sessionSkus:";
    public static final String SKUS_STOCK_SEMAPHORE = "seckill:stock:"; // + 商品随机码

    /**
     * 上架最近三天的秒杀商品信息
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1. 获取最近三天的秒杀活动信息
        R session = couponFeignService.getLatest3DaysSession();
        if (session.getCode() == 0) {
            // 收集秒杀活动信息
            List<SeckillSessionWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionWithSkus>>() {
            });
            // 在redis 当中缓存活动信息
            saveSessionInfo(sessionData);
            // 在redis 当中缓存秒杀商品  信息
            saveSessionSkusInfo(sessionData);
        }
    }

    /**
     * 保存秒杀活动信息
     *
     * @param sessionData
     */
    private void saveSessionInfo(List<SeckillSessionWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationEntities().stream().map(item -> item.getId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                // 在redis 当中缓存活动信息
                redisTemplate.opsForList().leftPush(key, collect);
            }

        });
    }

    /**
     * 保存秒杀商品信息
     *
     * @param sessionData
     */
    private void saveSessionSkusInfo(List<SeckillSessionWithSkus> sessionData) {
        BoundHashOperations hashOps = redisTemplate.boundHashOps(SESSIONS_SKUS_CACHE_PREFIX);
        sessionData.stream().forEach(session -> {

            SeckillSkuTo seckillSkuTo = new SeckillSkuTo();

            List<SeckillSkuVo> relationEntities = session.getRelationEntities();

            relationEntities.stream().forEach(relation -> {
                // 若 sku 不存在，则在redis 保存商品
                if (!hashOps.hasKey(relation.getId() + "_" + relation.getSkuId())) {
                    // 1. 保存 sku 基本信息
                    Long skuId = relation.getSkuId();
                    R info = productFeignService.info(skuId);
                    if (info.getCode() == 0) {
                        SkuInfo data = info.getData(new TypeReference<SkuInfo>() {
                        });
                        seckillSkuTo.setSkuInfo(data);
                    }
                    // 2. 保存 sku 秒杀信息
                    BeanUtils.copyProperties(relation, seckillSkuTo);

                    // 3. 设置当前商品的秒杀时间信息
                    seckillSkuTo.setStartTinme(session.getStartTime().getTime());
                    seckillSkuTo.setEndTime(session.getEndTime().getTime());
                    // 4. 商品秒杀随机码（防止攻击）
                    String randomCode = UUID.randomUUID().toString().replaceAll("-", "");
                    seckillSkuTo.setRandomCode(randomCode);

                    // 设置商品信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKUS_STOCK_SEMAPHORE + randomCode);
                    semaphore.trySetPermits(relation.getSeckillCount());

                    String jsonString = JSON.toJSONString(seckillSkuTo);
                    hashOps.put(relation.getId() + "_" + relation.getSkuId(), jsonString);
                }
            });

        });

    }
}
