package com.pfl.ssfmall.seckill.to;

import com.pfl.ssfmall.seckill.vo.SkuInfo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuTo {
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    private SkuInfo skuInfo;

    // 当前商品秒杀的开始时间
    private Long startTinme;
    // 当前商品秒杀的结束时间
    private Long endTime;

    /**
     * 商品秒杀随机码
     */
    private String randomCode;
}