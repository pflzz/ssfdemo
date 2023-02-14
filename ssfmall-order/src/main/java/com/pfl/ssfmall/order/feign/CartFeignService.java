package com.pfl.ssfmall.order.feign;

import com.pfl.ssfmall.order.model.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("ssfmall-cart")
public interface CartFeignService {
    @GetMapping("cartItem")
    List<OrderItemVo> getCartItems();
}
