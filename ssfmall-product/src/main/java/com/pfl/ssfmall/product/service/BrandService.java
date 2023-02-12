package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:34
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 级联更新 品牌 - 分类 关系表
     * @param brand
     */
    void updateCascade(BrandEntity brand);
}

