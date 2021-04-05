package com.linkknown.iwork.quartz;


import com.linkknown.iwork.core.Memory;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.util.JsonUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ScheduledJobService {

    // jobKey 字符串对应的实体类
    @Data
    @Accessors(chain = true)
    class JobKeyName {
        private String jobKey = "jobKey";
        private int appId;
        private String workName;
    }

    public static String toString(JobKeyName jobKeyName) {
        return JsonUtil.writeToString(jobKeyName);
    }

    public static JobKeyName toJobKeyName(String jobKeyName) {
        return JsonUtil.parseToObject(jobKeyName, JobKeyName.class);
    }

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobService.class);

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
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(JOB_GROUP);
        Set<JobKey> jobkeySet = scheduler.getJobKeys(matcher);
        List<JobKey> jobkeyList = new ArrayList<JobKey>();
        jobkeyList.addAll(
                jobkeySet.stream()
                        .filter(jobKey -> {
                            try {
                                int appId = toJobKeyName(jobKey.getName()).getAppId();
                                String workName = toJobKeyName(jobKey.getName()).getWorkName();
                                // 搜索 workCache 无效的 work
                                return Memory.getWorkCacheByNameFromMemory(appId, workName) == null;
                            } catch (Exception e) {
                                return true;
                            }
                        })
                        .collect(Collectors.toList())
        );
        // 清除无效的定时任务
        scheduler.deleteJobs(jobkeyList);

        // 从数据库中读取所有的定时任务流程
        List<Work> works = workService.queryWorksByWorkType(-1, "quartz");
        if (works != null) {
            for (Work work : works) {
                // 执行调度
                scheduleJobQuietly(work);
            }
        }
    }

    public void stop() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.clear();
    }

    public void deleteJob(Work work) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(
                new JobKeyName().setAppId(Integer.parseInt(work.getAppId())).setWorkName(work.getWorkName()).toString(),
                JOB_GROUP);
        boolean deleteJob = scheduler.deleteJob(jobKey);
        logger.info("remove job :" + deleteJob);
    }

    public void deleteJobQuietly(Work work) {
        try {
            this.deleteJob(work);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("deleteJobQuietly error:" + e);
        }
    }

    public void scheduleJob(Work work) throws SchedulerException {

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(
                new JobKeyName().setAppId(Integer.parseInt(work.getAppId())).setWorkName(work.getWorkName()).toString(),
                JOB_GROUP);
        JobDetail jobDetail = JobBuilder.newJob(ScheduledJobExecutor.class).withIdentity(jobKey).build();

        // 间隔执行（cron）执行一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(work.getWorkCron());
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(
                new JobKeyName().setAppId(Integer.parseInt(work.getAppId())).setWorkName(work.getWorkName()).toString(),
                JOB_GROUP).withSchedule(scheduleBuilder).build();
        cronTrigger.getJobDataMap().put("appId", work.getAppId());
        cronTrigger.getJobDataMap().put("workName", work.getWorkName());
        if (!scheduler.checkExists(jobKey)) {
            scheduler.scheduleJob(jobDetail, cronTrigger);
        }
    }

    public void scheduleJobQuietly(Work work) {
        if (StringUtils.equals(work.getWorkType(), "quartz") && StringUtils.isNotEmpty(work.getWorkCron())) {
            try {
                this.deleteJob(work);
                this.scheduleJob(work);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("scheduleJobQuietly error:" + e);
            }
        }
    }

}
