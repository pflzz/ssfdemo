package com.pfl.ssfmall.product.feign;

import com.pfl.common.to.SkuReductionTo;
import com.pfl.common.to.SpuBoundsTo;
import com.pfl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("ssfmall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveReduction(@RequestBody SkuReductionTo skuReductionTo);
}
