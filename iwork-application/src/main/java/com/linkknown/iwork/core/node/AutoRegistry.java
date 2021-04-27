package com.linkknown.iwork.core.node;

import java.lang.annotation.*;

/**
 * 节点自动注册
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface AutoRegistry {

}


