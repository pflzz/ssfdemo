package com.pfl.ssfmall.ware.exception;

public class NoStockException extends RuntimeException{
    public NoStockException(Long skuId) {
        super("商品id: " + skuId + ": 库存不足");
    }
}
