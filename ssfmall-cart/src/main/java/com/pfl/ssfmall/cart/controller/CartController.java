package com.pfl.ssfmall.cart.controller;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.cart.interceptor.CartInterceptor;
import com.pfl.ssfmall.cart.service.CartService;
import com.pfl.ssfmall.cart.vo.Cart;
import com.pfl.ssfmall.cart.vo.CartItem;
import com.pfl.ssfmall.cart.vo.UserInfoTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class CartController {

    @Resource
    private CartService cartService;


    /**
     * 获取当前用户的购物车条目
     */
    @GetMapping("cartItem")
    public List<CartItem> getCartItems() {
        return cartService.getUserCartItems();
    }
    /**
     * 获取当前用户购物车信息
     * @return
     */
    @GetMapping("/cart")
    public R cart() throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();
        return R.ok().put("data", cart);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/addCart")
    public R addCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addCart(skuId, num);
        return R.ok().put("data", cartItem);
    }

    /**
     * 选中购物车中条目
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public R checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return R.ok();
    }

    /**
     * 改变购物车条目的数量
     */
    @GetMapping("/changeCount")
    public R changeCount(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.changeCount(skuId, num);
        return R.ok();
    }
}
