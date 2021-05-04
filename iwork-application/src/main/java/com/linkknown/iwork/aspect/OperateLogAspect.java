package com.linkknown.iwork.aspect;

import com.linkknown.iwork.filter.IPFilter;
import com.linkknown.iwork.mapper.OperateLogMapper;
import com.linkknown.iwork.util.HttpUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperateLogAspect {

    @Autowired
    private OperateLogMapper operateLogMapper;

    private static final Logger logger = LoggerFactory.getLogger(OperateLogAspect.class);

    // 配置织入点
    @Pointcut("@annotation(com.linkknown.iwork.aspect.OperateLog)")
    public void logPointCut() {}

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    private void handleLog(JoinPoint joinPoint, Exception e, Object jsonResult) {
        OperateLog operateLog = getAnnotationLog(joinPoint);
        if (operateLog == null) {
            return;
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取 RequestAttributes 中获取 HttpServletRequest 的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        com.linkknown.iwork.entity.OperateLog _operateLog = new com.linkknown.iwork.entity.OperateLog();
        _operateLog.setLog(operateLog.log());
        _operateLog.setUserName((String) request.getSession().getAttribute("userName"));
        _operateLog.setIpAddr(HttpUtil.getIpAddress(request));
        _operateLog.setCreatedTime(new Date());
        operateLogMapper.insertOperateLog(_operateLog);
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OperateLog getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null)
        {
            return method.getAnnotation(OperateLog.class);
        }
        return null;
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }
}
