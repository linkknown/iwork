package com.linkknown.iwork.controller;

import com.linkknown.iwork.util.VerifyCodeImage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class LoginController {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${iwork.admin.account}")
    private String loginUserName;
    @Value("${iwork.admin.password}")
    private String loginPassWord;

    @RequestMapping("/login")
    public Object login(HttpServletRequest request) {
        String userName = request.getParameter("userName");
        String passwd = request.getParameter("passwd");
        String verifyCode = request.getParameter("verifyCode");

        Map<String, Object> resultMap = new HashMap<>();

        long expiredSecond = 3600 * 1;  // 默认 1 h 过期时间

        String _verifyCode = (String) request.getSession().getAttribute("verify_code");

        if (!StringUtils.equalsIgnoreCase(verifyCode, _verifyCode)) {
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", "验证码错误!");
            return resultMap;
        }

        if (loginUserName.equals(userName) && loginPassWord.equals(passwd)) {
            resultMap.put("status", "SUCCESS");
            resultMap.put("tokenString", "tokenString");
            resultMap.put("loginExpiredSecond", expiredSecond);

            //将验证码的文本存在 session 中
            request.getSession().setAttribute("userName", userName);
        } else {
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", "登陆失败，请检查用户名和密码是否正确!");
        }
        return resultMap;
    }

    /**
     * 生成验证码
     * @param request
     * @return
     */
    @RequestMapping("/genVerifyCode")
    public Object genVerifyCode(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 用我们的验证码类,生成验证码类对象
            VerifyCodeImage verifyCodeImage = new VerifyCodeImage();
            //获取验证码
            BufferedImage image = verifyCodeImage.getImage();
            //将验证码的文本存在 session 中
            request.getSession().setAttribute("verify_code", verifyCodeImage.getText());

            // 将验证码图片响应给客户端
            String png_base64 = verifyCodeImage.outputBase64PNG(image);

            resultMap.put("status", "SUCCESS");
            resultMap.put("img", png_base64);
        } catch (Exception e) {
            logger.error("验证码生成失败!", e);
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", "验证码生成失败!");
        }
        return resultMap;
    }
}