package com.linkknown.iwork.annotation;

import java.lang.annotation.*;

/**
 * 辅助助手注解，用于增强 iwork 框架，不具备任何执行能力，仅仅用来帮助说明
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
// 可重复添加注解
@Repeatable(value = HelpAssistantAnnotations.class)
public @interface HelpAssistantAnnotation {
    // 属性-描述
    String desc();
    // 属性-class 对象
    Class<?> clazz();
    // 属性-方法名
    HelpAssistantMethodEnum methodName() default HelpAssistantMethodEnum.EMPTY;
}


