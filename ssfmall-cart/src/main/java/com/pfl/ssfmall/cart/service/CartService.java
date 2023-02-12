package com.pfl.ssfmall.cart.service;

import com.pfl.ssfmall.cart.vo.Cart;
import com.pfl.ssfmall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    Cart getCart() throws ExecutionException, InterruptedException;

    void checkItem(Long skuId, Integer check);

    void changeCount(Long skuId, Integer num);

    List<CartItem> getUserCartItems();

}
