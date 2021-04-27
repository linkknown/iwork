package com.linkknown.iwork.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

//全局异常捕捉处理
@ControllerAdvice
public class GlobalExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object errorHandler(HttpServletRequest request, Exception e) throws Exception {
        logger.error("http 请求全局异常拦截：", e);

        Map<String, Object> map = new HashMap<>();
        map.put("exception", String.format("【GlobalExceptionHandler】%s", e.toString()));
        map.put("url", request.getRequestURL().toString());

        return map;
    }

}
