package com.pfl.ssfmall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的检索条件
 */
@Data
public class SearchParam {
    private String keyword; // 从页面传递过来的全文匹配关键字
    private Long catalog3Id;

    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum;

}
