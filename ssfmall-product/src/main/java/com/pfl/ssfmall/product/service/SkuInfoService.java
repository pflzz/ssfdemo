package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.SkuInfoEntity;
import com.pfl.ssfmall.product.vo.SkuItemSaleAttrVo;
import com.pfl.ssfmall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:28
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * sku 条件检索
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据 spuId 查询 sku 基本信息
     * @param spuId
     * @return
     */
    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    /**
     * 获取商品详情页面渲染数据
     * @param skuId 指定的商品
     * @return
     */
    SkuItemVo getSkuItem(Long skuId) throws ExecutionException, InterruptedException;


    /**
     * 获取 spu 所有的销售属性及其属性值
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrVo> getSkuItemSaleAttr(Long spuId);
}

