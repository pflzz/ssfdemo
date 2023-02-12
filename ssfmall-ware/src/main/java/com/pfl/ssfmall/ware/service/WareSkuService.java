package com.pfl.ssfmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.ware.entity.WareSkuEntity;
import com.pfl.ssfmall.ware.vo.SkuStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:22:38
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 完成采购后 添加 / 修改 库存信息
     * @param skuId 采购项 id
     * @param wareId 仓库 id
     * @param skuNum 采购数量
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 检索该商品是否有库存
     */
    List<SkuStockVo> hasStock(List<Long> skuIds);
}

