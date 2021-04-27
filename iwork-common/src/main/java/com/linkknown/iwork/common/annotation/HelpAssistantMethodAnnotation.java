package com.linkknown.iwork.common.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@Documented
public @interface HelpAssistantMethodAnnotation {
    // 属性-描述
    HelpAssistantMethodEnum methodName();
}


