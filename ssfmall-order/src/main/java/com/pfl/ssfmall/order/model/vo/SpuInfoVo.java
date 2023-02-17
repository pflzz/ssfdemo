package com.pfl.ssfmall.order.model.vo;


import lombok.Data;

@Data
public class SpuInfoVo {
    // 品牌信息
    private Long brandId;

    // 商品分类信息
    private Long catalogId;

    // spuid
    private Long id;

    // spu_name 商品名字
    private String spuName;

}
