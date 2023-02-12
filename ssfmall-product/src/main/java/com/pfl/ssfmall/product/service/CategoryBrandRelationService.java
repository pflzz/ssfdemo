package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.BrandEntity;
import com.pfl.ssfmall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:28
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存除前端传递以外的数据，表的详细数据
     * @param categoryBrandRelation 前端传递的数据
     */
    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCatelog(Long catId, String name);

    /**
     * 获取分类关联的品牌
     * @param catId 分类id
     * @return
     */
    List<BrandEntity> getBrandListByCatId(Long catId);
}

