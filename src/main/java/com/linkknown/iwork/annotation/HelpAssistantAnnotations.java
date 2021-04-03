package com.linkknown.iwork.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface HelpAssistantAnnotations {
    HelpAssistantAnnotation[] value();
}
