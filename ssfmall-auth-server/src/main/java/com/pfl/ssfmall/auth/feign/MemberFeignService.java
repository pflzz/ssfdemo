package com.pfl.ssfmall.auth.feign;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.auth.vo.SocialUser;
import com.pfl.ssfmall.auth.vo.UserLoginVo;
import com.pfl.ssfmall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("ssfmall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/socialLogin")
    R socialLogin(@RequestBody SocialUser user) throws Exception;
}
