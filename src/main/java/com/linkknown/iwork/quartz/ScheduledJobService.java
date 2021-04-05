package com.linkknown.iwork.quartz;


import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.service.WorkService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledJobService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobService.class);

    private static final String JOB_KEY = "jobKey";
    private static final String JOB_GROUP = "jobGroup";

    @Autowired
    private WorkService workService;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    public ScheduledJobService(SchedulerFactoryBean schedulerFactoryBean) {
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    /**
     * 该方法用来启动所有的定时任务
     *
     * @throws SchedulerException
     */
    public void scheduleJobs() throws SchedulerException {
        // 从数据库中读取所有的定时任务流程
        List<Work> works = workService.queryWorksByWorkType(-1, "quartz");
        if (works != null) {
            for (Work work : works) {
                // 执行调度
                scheduleJob(work);
            }
        }

    }

    public void stop() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.clear();
    }

    public void remove(String workId) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        boolean deleteJob = scheduler.deleteJob(new JobKey(JOB_KEY + workId, JOB_GROUP));
        logger.info("remove job :" + deleteJob);
    }

    public void scheduleJob(Work work) throws SchedulerException {

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(JOB_KEY + work.getId(), JOB_GROUP);
        JobDetail jobDetail = JobBuilder.newJob(ScheduledJobExecutor.class) .withIdentity(jobKey).build();

        // 间隔执行（cron）执行一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(work.getWorkCron());
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(JOB_KEY + work.getId(), JOB_GROUP).withSchedule(scheduleBuilder).build();
        cronTrigger.getJobDataMap().put("work", work);
        if (!scheduler.checkExists(jobKey)) {
            scheduler.scheduleJob(jobDetail, cronTrigger);
        }
    }

}
