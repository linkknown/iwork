package com.linkknown.iwork.quartz;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ScheduledJobLisenter implements ApplicationListener<ContextRefreshedEvent> {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobLisenter.class);

    @Resource
    private ScheduledJobService scheduledJobService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("开始加载 quartz 任务");
        try {
            scheduledJobService.scheduleJobs();
        } catch (SchedulerException e) {
            logger.error("加载 quartz 任务失败！", e);
        }
    }
}
