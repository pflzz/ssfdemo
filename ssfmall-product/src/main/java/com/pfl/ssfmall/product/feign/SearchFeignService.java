package com.pfl.ssfmall.product.feign;

import com.pfl.common.to.SkuEsModel;
import com.pfl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("ssfmall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productUp(@RequestBody List<SkuEsModel> skuEsModels);
}
