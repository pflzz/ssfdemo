package com.pfl.ssfmall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// 订单确认页数据
public class OrderConfirmVo {
    // 收货地址
    @Getter @Setter
    List<MemberAddressVo> address;

    // 所选中购物项
    @Getter @Setter
    List<OrderItemVo> items;


    /**
     * 积分
     */
    @Getter @Setter
    private Integer integration;



    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }


    public BigDecimal getPayPrice() {
        return getTotal();
    }

    /**
     * 防重令牌
     */
    @Getter @Setter
    String orderToken;
}