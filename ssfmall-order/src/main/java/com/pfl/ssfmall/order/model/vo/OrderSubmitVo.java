package com.pfl.ssfmall.order.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addressId; // 用户收货地址id
    private Integer payType; // 支付方式
    // 无需提交需要购买的商品，去购物车再去获取一遍
    // 优惠 发票
    private String orderToken; // 防重令牌
    private BigDecimal payPrice; // 应付价格
}
