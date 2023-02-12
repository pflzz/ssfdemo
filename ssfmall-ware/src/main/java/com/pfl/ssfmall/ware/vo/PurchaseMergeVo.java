package com.pfl.ssfmall.ware.vo;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseMergeVo {
    /**
     * 整单id
     */
    private Long purchaseId;
    /**
     * 合并项集合
     */
    private List<Long> items;
}
