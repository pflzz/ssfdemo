package com.pfl.ssfmall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pfl.common.constant.AuthServerConstant;
import com.pfl.common.exception.BizCodeEnum;
import com.pfl.common.utils.HttpUtils;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.auth.feign.MemberFeignService;
import com.pfl.ssfmall.auth.vo.MemberEntityVo;
import com.pfl.ssfmall.auth.vo.SocialUser;
import com.pfl.ssfmall.auth.vo.UserLoginVo;
import com.pfl.ssfmall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class LoginController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MemberFeignService memberFeignService;

    /**
     * 给手机发送验证码
     *
     * @param phone
     * @return
     */
    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // todo 1. 调用第三方服务给用户手机发送验证码

        // 2. 在 redis 中缓存验证码 防止一个手机号在限定时间中重复多次发送验证码请求

        String key = AuthServerConstant.SMS_CODE_PREFIX + phone;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(code)) {
            long currentTimeMillis = System.currentTimeMillis();
            // 判断发送间隔是否大于60s
            if (currentTimeMillis - Long.parseLong(code.split("_")[1]) < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String sendCode = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(key, sendCode, 10, TimeUnit.MINUTES);


        return R.ok();
    }

    /**
     * 处理注册请求
     *
     * @return
     */
    @PostMapping("/register")
    public R register(@Valid UserRegisterVo userRegisterVo, BindingResult result) {

        // 1. 前置校验
        if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach((item) -> {
                map.put(item.getField(), item.getDefaultMessage());
            });
            return R.error(400, "提交的数据不合法").put("data", map);
        }
        // 2.1 校验验证码
        String key = AuthServerConstant.SMS_CODE_PREFIX + userRegisterVo.getPhone();
        String code = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(code)) {
            code = code.split("_")[0];
            // 验证码通过
            if (code.equals(userRegisterVo.getCode())) {
                // 删除验证码
                stringRedisTemplate.delete(key);
                // 2.2 调用远程服务完成注册功能
                R r = memberFeignService.register(userRegisterVo);
                if (r.getCode() != 0) {
                    return R.error(r.getCode(), r.getMsg());
                }


            } else {
                return R.error(400, "验证码错误");
            }
        } else {
            return R.error(400, "请重新发送验证码");
        }


        return R.ok();
    }

    /**
     * 登录
     *
     * @return
     */
    @PostMapping("/login")
    public R login(UserLoginVo vo, HttpSession session) {

        R login = memberFeignService.login(vo);
        if (login.getCode() != 0) {
            return R.error(login.getCode(), login.getMsg());
        } else {
            return R.ok();
        }

    }

    @PostMapping("/socialLogin")
    public R socialLogin(@RequestParam("code") String code, HttpSession session) throws Exception {

        // 构造请求 获取 access token
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "3173900243");
        map.put("client_secret", "97b9f510ecb92a69dcfcf4c261cf229c");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://baidu.com");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), null, map);
        // 如果返回结果正常
        if (response.getStatusLine().getStatusCode() == 200) {
            // 处理
            // 将响应体转化为 json 字符串
            String json = EntityUtils.toString(response.getEntity());

            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            R socialLogin = memberFeignService.socialLogin(socialUser);
            if (socialLogin.getCode() == 0) {
                MemberEntityVo data = socialLogin.getData("data", new TypeReference<MemberEntityVo>() {
                });
                log.info("用户 {} 登录成功", data);

                session.setAttribute("loginUser", data);
                return R.ok().put("data", data);
            } else {
                return R.error(BizCodeEnum.WEIBO_AUTH_EXCEPTION.getCode(), BizCodeEnum.WEIBO_AUTH_EXCEPTION.getMessage());
            }

        } else {
            return R.error(BizCodeEnum.WEIBO_AUTH_EXCEPTION.getCode(), BizCodeEnum.WEIBO_AUTH_EXCEPTION.getMessage());
        }

    }
}
