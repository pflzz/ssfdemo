package com.pfl.ssfmall.cart.feign;

import com.pfl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("ssfmall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
        //@RequiresPermissions("product:skuinfo:info")
    R info(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/skusaleattrvalue/saleAttrValue/{skuId}")
    List<String> getSaleAttrNameWithValuesAsList(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfoprice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
