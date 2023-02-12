package com.pfl.ssfmall.product.dao;

import com.pfl.ssfmall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:28
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<String> getSaleAttrNameWithValuesAsList(@Param("skuId") Long skuId);
}
