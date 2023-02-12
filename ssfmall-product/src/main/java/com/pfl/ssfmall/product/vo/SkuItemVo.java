package com.pfl.ssfmall.product.vo;

import com.pfl.ssfmall.product.entity.SkuImagesEntity;
import com.pfl.ssfmall.product.entity.SkuInfoEntity;
import com.pfl.ssfmall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    private SkuInfoEntity info;
    private List<SkuImagesEntity> images;
    private SpuInfoDescEntity desc;
    private List<SkuItemSaleAttrVo> saleAttr;
    private List<SpuItemGroupAttrVo> groupAttr;
}
