package com.linkknown.iwork.common.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface HelpAssistantAnnotations {
    HelpAssistantAnnotation[] value();
}
