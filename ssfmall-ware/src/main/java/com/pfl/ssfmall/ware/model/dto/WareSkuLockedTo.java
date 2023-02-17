package com.pfl.ssfmall.ware.model.dto;

import com.pfl.ssfmall.ware.model.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockedTo {
    private String OrderSn;
    private List<OrderItemVo> locks;
}
