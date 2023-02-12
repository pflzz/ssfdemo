package com.pfl.ssfmall.seckill.feign;

import com.pfl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("ssfmall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsessionlatest3DaysSession")
    R getLatest3DaysSession();
}
