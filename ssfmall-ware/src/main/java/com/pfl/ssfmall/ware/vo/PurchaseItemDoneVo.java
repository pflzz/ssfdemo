package com.pfl.ssfmall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PurchaseItemDoneVo {
    /**
     * itemId:1,status:4,reason:""
     */

    /**
     * 采购项 id
     */
    @NotNull
    private Long itemId;
    /**
     * 采购状态
     */
    @NotNull
    private Integer status;
    /**
     * 原因
     */
    private String reason;
}
