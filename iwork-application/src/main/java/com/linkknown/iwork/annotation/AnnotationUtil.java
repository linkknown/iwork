package com.linkknown.iwork.annotation;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.Set;

public class AnnotationUtil {

    /**
     * 使用参考
     * https://blog.csdn.net/qq_36838191/article/details/80507206
     * https://blog.csdn.net/weixin_38362455/article/details/80409574
     *
     * @param packageName 要扫描的包
     * @param clazz       要扫描的注解
     */
    public static Set<Class<?>> scan(String packageName, Class<? extends Annotation> clazz) {

        ConfigurationBuilder config = new ConfigurationBuilder();
//        config.filterInputsBy(new FilterBuilder().includePackage("com.gcol.qy.web.system.api").includePackage("com.gcol.qy.web.system.controller"));
        config.addUrls(ClasspathHelper.forPackage(packageName));
        config.setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
        Reflections reflections = new Reflections(config);

        return reflections.getTypesAnnotatedWith(clazz);
    }
}
