package com.linkknown.iwork.aspect;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 日志
     */
    String log() default "";

}
