package com.pfl.ssfmall.order.feign;

import com.pfl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("ssfmall-product")
public interface ProductFeignService {
    @GetMapping("/spuInfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
