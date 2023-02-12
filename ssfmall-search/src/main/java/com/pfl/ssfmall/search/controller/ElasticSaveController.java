package com.pfl.ssfmall.search.controller;

import com.pfl.common.exception.BizCodeEnum;
import com.pfl.common.to.SkuEsModel;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.search.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Resource
    private ElasticSaveService elasticSaveService;

    /**
     * 商品上架
     *
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean hasFailures;
        try {
            hasFailures = elasticSaveService.productUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController 商品上架错误：{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (!hasFailures) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
    }
}
