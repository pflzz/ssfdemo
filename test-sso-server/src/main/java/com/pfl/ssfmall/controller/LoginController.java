package com.pfl.ssfmall.controller;

import com.sun.corba.se.impl.oa.toa.TOA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/userInfo")
    public String login(@RequestParam("token") String token) {
        if (!StringUtils.isEmpty(token)) {
            String s = redisTemplate.opsForValue().get(token);
            return s;
        }
        return null;
    }


    @GetMapping("/login.html")
    public String login(@RequestParam("redirect_url") String url, Model model,
                        @CookieValue(value = "sso_token", required = false) String sso_token) {
        if (!StringUtils.isEmpty(sso_token)) {
            return "redirect:" + url + "?token=" + sso_token;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("doLogin")
    public String doLogin(String username, String password, String url, HttpServletResponse response) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(uuid, username);
            response.addCookie(new Cookie("sso_token", uuid));
            return "redirect:" + url + "?token=" + uuid;
        }

        return "login";
    }

}
