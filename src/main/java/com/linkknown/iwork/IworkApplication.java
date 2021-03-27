package com.linkknown.iwork;

import com.linkknown.iwork.listener.ApplicationStartupListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

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
