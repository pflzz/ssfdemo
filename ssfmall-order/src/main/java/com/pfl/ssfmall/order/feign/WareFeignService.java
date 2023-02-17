package com.pfl.ssfmall.order.feign;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.order.model.dto.WareSkuLockedTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("ssfmall-ware")
public interface WareFeignService {
    @PostMapping("/orderLockStock")
    R orderLockStock(WareSkuLockedTo wareSkuLockedVo);

    @GetMapping("/getFare")
    R getFare(Long addressId);
}
