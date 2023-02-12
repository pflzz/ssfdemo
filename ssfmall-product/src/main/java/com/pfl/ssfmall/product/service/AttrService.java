package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.AttrEntity;
import com.pfl.ssfmall.product.entity.ProductAttrValueEntity;
import com.pfl.ssfmall.product.vo.AttrResponseVo;
import com.pfl.ssfmall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:34
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存属性和属性分组之间的关联信息
     *
     * @param attrVo 属性信息
     */
    void saveAttrVo(AttrVo attrVo);

    PageUtils getBaseAttrList(Map<String, Object> params, Long catelogId, String attrType);


    AttrResponseVo getAttrDetails(Long attrId);

    void updateAttr(AttrVo attr);

    /**
     * 获取分组下所有的属性
     *
     * @param attrgroupId 分组id
     * @return
     */
    List<AttrEntity> getAttrRelation(Long attrgroupId);

    /**
     * 获取属性分组里面 还没有 关联的本分类里面的其他基本属性，方便添加新的关联
     *
     * @param params      分页数据
     * @param attrgroupId 分组id
     * @return
     */
    PageUtils getNoattrRelation(Map<String, Object> params, Long attrgroupId);


}

