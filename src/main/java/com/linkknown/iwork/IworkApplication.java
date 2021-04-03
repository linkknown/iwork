package com.linkknown.iwork;

import com.linkknown.iwork.annotation.HelpAssistantAnnotation;
import com.linkknown.iwork.annotation.HelpAssistantMethodEnum;
import com.linkknown.iwork.controller.HttpServiceController;
import com.linkknown.iwork.core.Regist;
import com.linkknown.iwork.listener.ApplicationStartupListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@HelpAssistantAnnotation(desc = "应用启动监听器,可以在此类中执行初始化操作", clazz = ApplicationStartupListener.class)
@HelpAssistantAnnotation(desc = "iwork框架节点注册类", clazz = Regist.class)
@HelpAssistantAnnotation(desc = "iwork框架发布服务控制器类", clazz = HttpServiceController.class, methodName = HelpAssistantMethodEnum.HTTP_SERVICE)

@SpringBootApplication
@ServletComponentScan
@MapperScan("com.linkknown.iwork.dao")
public class IworkApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IworkApplication .class);
        springApplication.addListeners(new ApplicationStartupListener());
        springApplication.run(args);
    }

}
