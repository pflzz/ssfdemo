package com.pfl.ssfmall.product.dao;

import com.pfl.ssfmall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pfl.ssfmall.product.vo.SpuItemGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:34
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
    /**
     * 根据分类 id 和 spuId 获取该分类（spu）属性分组及其属性值列表
     * @param catalogId
     * @param spuId
     * @return
     */
    List<SpuItemGroupAttrVo> getAttrGroupWithAttr(@Param("catalogId") Long catalogId, @Param("spuId") Long spuId);

    /**
     * 查询还没有 关联的本分类里面的其他 基本属性
     * @param attrIds  已有的属性id
     * @return
     */
}
