package com.pfl.ssfmall.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class HelloController {


    @Value("${sso.server.url}")
    private String ssoServerUrl;
    @ResponseBody
    @GetMapping
    public String hello() {
        return "hello";
    }

    @GetMapping("employees")
    public String employee(Model model,
                           HttpSession session,
                           @RequestParam(value = "token", required = false) String token) {

        if (!StringUtils.isEmpty(token)) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity("http:ssoserver.com:8080/userInfo?token" + token, String.class);
            String body = response.getBody();
            session.setAttribute("loginUser", body);
        }
        ArrayList<String> employees = new ArrayList<>();
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser != null) {
            // 已登录
            employees.add("uzi");
            employees.add("faker");
            model.addAttribute("emp", employees);
            return "list";
        } else {
            // 未登录，跳转到登录服务器的页面
            // 在地址后面添加查询参数（从哪个服务器的页面来）
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        }

    }
}
