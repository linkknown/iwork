package com.linkknown.iwork.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

// quartz 使用教程：参考 https://blog.csdn.net/u010176542/article/details/79774470
@Configuration
public class QuartzConfig {

    private final static Logger logger = LoggerFactory.getLogger(QuartzConfig.class);

    /**
     * iwork 框架统一数据源
     */
    @Autowired
    private DataSource dataSource;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Bean("taskExecutor")
    @Primary
    @Order(1)
    public ThreadPoolTaskExecutor getThreadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(50);
        threadPoolTaskExecutor.setCorePoolSize(20);
        RejectedExecutionHandler handler = (r, executor) -> {
            logger.error("线程池超过最大容量,进行扩容 {} ",executor.getPoolSize());
            executor.setMaximumPoolSize(10);
            Thread thread = new Thread(r);
            thread.start();
            logger.error(r.toString());
        };
        threadPoolTaskExecutor.setRejectedExecutionHandler(handler);
        return threadPoolTaskExecutor;
    }

    @Bean
    @Order(2)
    public SchedulerFactoryBean getSchedulerFactoryBean() throws IOException {
        //获取配置属性
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));

        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();

        //创建SchedulerFactoryBean
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setTaskExecutor(threadPoolTaskExecutor);
        factory.setQuartzProperties(propertiesFactoryBean.getObject());
        // 使用数据源
        factory.setDataSource(dataSource);

        //这样当spring关闭时，会等待所有已经启动的quartz job结束后spring才能完全shutdown。
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setOverwriteExistingJobs(false);
        factory.setStartupDelay(1);
        return factory;
    }
}
