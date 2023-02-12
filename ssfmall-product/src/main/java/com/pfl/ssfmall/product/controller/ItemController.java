package com.pfl.ssfmall.product.controller;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.product.service.SkuInfoService;
import com.pfl.ssfmall.product.vo.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * 商品详情页面请求
 */
@RestController
@RequestMapping("/product/item")
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 获取商品详情页面渲染数据
     * @param skuId 指定的商品
     * @return
     */
    @GetMapping("/{skuId}.html")
    public R skuItem(@PathVariable("skuId") Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo res = skuInfoService.getSkuItem(skuId);
        return R.ok().put("data", res);
    }
}
