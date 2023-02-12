package com.pfl.ssfmall.product.dao;

import com.pfl.ssfmall.product.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pfl.ssfmall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku信息
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:28
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {

    /**
     * 获取 spu 所有的销售属性及其属性值
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrVo> getSkuItemSaleAttr(@Param("spuId") Long spuId);
}
