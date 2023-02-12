package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:27
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取spu规格
     * @param spuId
     * @return
     */
    List<ProductAttrValueEntity> baseListForSpu(Long spuId);

    /**
     * 根据 spuId 修改商品规格
     * @param spuId
     * @param list
     * @return
     */
    void updateBySpuId(Long spuId, List<ProductAttrValueEntity> list);

}

