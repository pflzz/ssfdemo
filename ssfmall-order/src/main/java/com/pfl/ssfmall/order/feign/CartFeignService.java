package com.pfl.ssfmall.order.feign;

import com.pfl.ssfmall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("ssfmall-cart")
public interface CartFeignService {
    @GetMapping("cartItem")
    public List<OrderItemVo> getCartItems();
}
