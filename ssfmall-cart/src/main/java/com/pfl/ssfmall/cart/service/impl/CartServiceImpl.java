package com.pfl.ssfmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.cart.constant.CartConstant;
import com.pfl.ssfmall.cart.feign.ProductFeignService;
import com.pfl.ssfmall.cart.interceptor.CartInterceptor;
import com.pfl.ssfmall.cart.service.CartService;
import com.pfl.ssfmall.cart.vo.Cart;
import com.pfl.ssfmall.cart.vo.CartItem;
import com.pfl.ssfmall.cart.vo.SkuInfoEntity;
import com.pfl.ssfmall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    @Override
    public CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        // 1. 获取当前（正常 / 临时）用户的购物车
        BoundHashOperations<String, Object, Object> operations = getUserCart();


        String res = (String) operations.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 添加新商品到购物车
            CartItem cartItem = new CartItem();
            // 2. 远程查询 skuInfo 信息
            R info = productFeignService.info(skuId);
            SkuInfoEntity skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoEntity>() {
            });
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                cartItem.setSkuId(skuId);
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
            }, threadPoolExecutor);


            CompletableFuture<Void> getSkuSaleAttrTask = CompletableFuture.runAsync(() -> {
                // 3. 远程查询 sku 销售属性
                List<String> list = productFeignService.getSaleAttrNameWithValuesAsList(skuId);
                cartItem.setSkuAttr(list);
            }, threadPoolExecutor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrTask).get();
            //4. 将封装好的购物车条目数据存入 redis 中用户购物车
            String jsonString = JSON.toJSONString(cartItem);
            operations.put(skuId.toString(), jsonString);
            return cartItem;
        } else {
            // 修改原有商品的数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            // 将修改后的信息存入 res
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        // 1. 获取当前用户
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        // 登录用户
        if (userInfoTo.getUserId() != null) {
            Cart cart = new Cart();
            String cartKey = CartConstant.USER_CART_PREFIX + userInfoTo.getUserId();
            // 将临时购物车的数据合并到登录购物车
            String tmpCartKey = CartConstant.USER_CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tmpCartItems = getCartItems(tmpCartKey);
            if (tmpCartItems != null && tmpCartItems.size() > 0) {
                for (CartItem cartItem : tmpCartItems) {
                    addCart(cartItem.getSkuId(), cartItem.getCount());
                }
            }
            // 清空临时购物车
            stringRedisTemplate.delete(tmpCartKey);
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
            return cart;
        } else {
            // 临时用户
            String cartKey = CartConstant.USER_CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            Cart cart = new Cart();
            cart.setItems(cartItems);
            return cart;
        }


    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> userCart = getUserCart();
        CartItem cartItem = (CartItem) userCart.get(skuId);
        if (cartItem != null) {
            cartItem.setCheck(check == 1);
        }
        String jsonString = JSON.toJSONString(cartItem);
        userCart.put(skuId.toString(), jsonString);
    }

    @Override
    public void changeCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> userCart = getUserCart();
        CartItem o = (CartItem) userCart.get(skuId);
        o.setCount(o.getCount() + num);
        userCart.put(skuId.toString(), JSON.toJSONString(o));
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo == null) {
            return null;
        } else {
            String cartKey = CartConstant.USER_CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            return cartItems.stream().filter(item -> item.getCheck()).map(item -> {
                // 获取商品最新价格
                BigDecimal price = productFeignService.getPrice(item.getSkuId());
                item.setPrice(price);
                return item;
            }).collect(Collectors.toList());
        }
    }

    /**
     * 获取购物车列表条目
     *
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        // 购物车中有数据
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> JSON.parseObject((String) obj, CartItem.class)).collect(Collectors.toList());
            return collect;

        }

        return null;

    }

    /**
     * 获取当前（正常 / 临时）用户的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getUserCart() {
        // 获取当前用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        // 1. 从 redis 中获取用户的购物车

        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CartConstant.USER_CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CartConstant.USER_CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
