package com.pfl.ssfmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.to.SkuReductionTo;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 23:03:48
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存 sku 的优惠、满减等信息
     * @return
     */
    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

