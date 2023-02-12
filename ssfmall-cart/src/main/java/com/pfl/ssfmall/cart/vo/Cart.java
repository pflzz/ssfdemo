package com.pfl.ssfmall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车数据模型
 */
public class Cart {
    private List<CartItem> items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        for (CartItem cartItem : items) {
            count += cartItem.getCount();
        }
        return count;
    }


    public Integer getCountType() {
        int count = 0;
        for (CartItem cartItem : items) {
            count++;
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (items != null && items.size() < 0) {
            for (CartItem cartItem : items) {
                if (cartItem.getCheck()) {
                    amount.add(cartItem.getTotalPrice());
                }
            }
        }
        amount.subtract(reduce);
        return amount;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
