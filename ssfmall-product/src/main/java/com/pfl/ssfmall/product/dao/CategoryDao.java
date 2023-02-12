package com.pfl.ssfmall.product.dao;

import com.pfl.ssfmall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:33
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
