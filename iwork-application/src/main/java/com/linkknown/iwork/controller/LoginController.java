package com.linkknown.iwork.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class LoginController {

    @Value("${iwork.admin.account}")
    private String loginUserName;
    @Value("${iwork.admin.password}")
    private String loginPassWord;

    @RequestMapping("/login")
    public Object login(HttpServletRequest request) {
        String username = request.getParameter("username");
        String passwd = request.getParameter("passwd");

        Map<String, Object> resultMap = new HashMap<>();

        long expiredSecond = 3600 * 1;  // 默认 1 h 过期时间

        if (loginUserName.equals(username) && loginPassWord.equals(passwd)) {
            resultMap.put("status", "SUCCESS");
            resultMap.put("tokenString", "tokenString");
            resultMap.put("loginExpiredSecond", expiredSecond);
        } else {
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", "login error!");
        }
        return resultMap;
    }
}