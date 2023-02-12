package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.AttrEntity;
import com.pfl.ssfmall.product.entity.AttrGroupEntity;
import com.pfl.ssfmall.product.vo.AttrGroupWithAttrsVo;
import com.pfl.ssfmall.product.vo.SpuItemGroupAttrVo;


import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:34
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据 三级分类 id 获取对应属性分组信息
     * @param params 分页格式信息
     * @param catelogId 三级分类 id
     * @return
     */
    PageUtils queryCategoryPage(Map<String, Object> params, Long catelogId);

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId 分类 id
     * @return
     */
    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrs(Long catelogId);

    /**
     * 根据分类 id 和 spuId 获取该分类（spu）属性分组及其属性值列表
     * @param catalogId
     * @param spuId
     * @return
     */
    List<SpuItemGroupAttrVo> getAttrGroupWithAttr(Long catalogId, Long spuId);
}

