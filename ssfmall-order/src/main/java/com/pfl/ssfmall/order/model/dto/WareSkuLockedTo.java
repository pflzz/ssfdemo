package com.pfl.ssfmall.order.model.dto;

import com.pfl.ssfmall.order.model.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockedTo {
    private String OrderSn;
    private List<OrderItemVo> locks;
}
