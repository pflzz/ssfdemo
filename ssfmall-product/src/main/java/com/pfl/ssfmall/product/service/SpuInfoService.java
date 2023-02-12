package com.pfl.ssfmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.product.entity.SpuInfoEntity;
import com.pfl.ssfmall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 16:24:28
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存 spu info
     * @param vo
     */
    void saveSpuInfo(SpuSaveVo vo);

    /**
     * spu 检索
     */
    PageUtils queryPageDetails(Map<String, Object> params);

    /**
     * 商品上架
     * @param spuId
     * @return
     */
    void up(Long spuId);
}

