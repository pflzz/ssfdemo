package com.pfl.ssfmall.order.feign;

import com.pfl.ssfmall.order.model.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("ssfmall-member")
public interface MemberFeignService {
    @GetMapping("/getAddress/{memberId}")
     List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
