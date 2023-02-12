package com.pfl.ssfmall.product.dao;

import com.pfl.ssfmall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:34
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatch(@Param("attrGroupRelationEntity") List<AttrAttrgroupRelationEntity> attrGroupRelationEntity);
}
