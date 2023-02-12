package com.pfl.ssfmall.product.dao;

import com.pfl.ssfmall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:28
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateCatelog(@Param("catId") Long catId, @Param("name") String name);
}
