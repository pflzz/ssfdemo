package com.pfl.ssfmall.order.feign;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.order.model.dto.WareSkuLockedTo;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ssfmall-ware")
public interface WareFeignService {
    R orderLockStock(WareSkuLockedTo wareSkuLockedVo);
}
