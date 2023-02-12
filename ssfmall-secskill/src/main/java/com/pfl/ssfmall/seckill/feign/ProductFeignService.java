package com.pfl.ssfmall.seckill.feign;

import com.pfl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("ssfmall-product")
public interface ProductFeignService {
    @GetMapping("/product/skuinfo/info/{skuId}")
     R info(@PathVariable("skuId") Long skuId);
}
