package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.CategoryEntity;
import com.pfl.ssfmall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:33
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询所有分类及其子分类，并以树形结构组装起来
     */
    List<CategoryEntity> getCategoryWithTree();

    /**
     * 获取 catelogId 分类完整的父路径信息
     * @param catelogId 目标分类
     * @return
     */
    Long[] getFullPath(Long catelogId);

    void updateCascade(CategoryEntity category);


    /**
     * 获取所有一级分类
     * @return
     */
    List<CategoryEntity> getLevel1List();

    /**
     * 获取 2，3 子分类数据
     */
    Map<Long, List<Catelog2Vo>> getCatalogJson();


}

