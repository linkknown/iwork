package com.linkknown.iwork.aspect;

import com.linkknown.iwork.util.HttpUtil;
import com.linkknown.iwork.util.JsonUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class HttpAccessAspect {

    private final static Logger logger = LoggerFactory.getLogger(HttpAccessAspect.class);

    @Data
    @Accessors(chain = true)
    static class HttpAccessLog {
        private String accessIp;        // 访问 IP
        private String methodUrl;       // 请求地址
        private String methodParams;    // 请求参数
        private int status;             // 响应吗
    }

    private static ThreadLocal<HttpAccessLog> threadLocal = new ThreadLocal<>();

    @Pointcut("execution(public * com.linkknown.iwork.controller.HttpServiceController.httpservice(..))")
    public void httpservice() {

    }

    /**
     * Aop 前置通知, 记录接口访问信息
     *
     * @param joinPoint
     */
    @Before("httpservice()")
    public void before(JoinPoint joinPoint) {
//        //获取目标方法的参数信息
//        Object[] obj = joinPoint.getArgs();
//        //AOP代理类的信息
//        joinPoint.getThis();
//        //代理的目标对象
//        joinPoint.getTarget();
//        //用的最多 通知的签名
//        Signature signature = joinPoint.getSignature();
//        //代理的是哪一个方法
//        logger.info("代理的是哪一个方法" + signature.getName());
//        //AOP代理类的名字
//        logger.info("AOP代理类的名字" + signature.getDeclaringTypeName());
//        //AOP代理类的类（class）信息
//        signature.getDeclaringType();

        //获取 RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取 RequestAttributes 中获取 HttpServletRequest 的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        //获取请求参数
        Map<String, String> parameterMap = resolveParameterMap(request);

        HttpAccessLog accessLog = new HttpAccessLog();
        accessLog.setMethodParams(JsonUtil.writeToString(parameterMap));
        accessLog.setAccessIp(HttpUtil.getIpAddress(request));
        accessLog.setMethodUrl(request.getRequestURI());

        threadLocal.set(accessLog);
    }

    @After("httpservice()")
    public void after (JoinPoint joinPoint) {
        try {
            //获取 RequestAttributes
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            HttpServletResponse httpServletResponse = ((ServletRequestAttributes) requestAttributes).getResponse();
            int status = httpServletResponse.getStatus();

            HttpAccessLog accessLog = threadLocal.get();
            accessLog.setStatus(status);

            logger.info(JsonUtil.writeToString(accessLog));
        } finally {
            threadLocal.remove();
        }
    }

    /**
     * 获取请求参数
     *
     * @param request
     * @return
     */
    private Map<String, String> resolveParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();

        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String parameter = enumeration.nextElement();
            parameterMap.put(parameter, request.getParameter(parameter));
        }
        return parameterMap;
    }
}
