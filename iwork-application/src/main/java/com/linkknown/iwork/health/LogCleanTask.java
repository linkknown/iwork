package com.linkknown.iwork.health;

import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.service.RunLogService;
import com.linkknown.iwork.service.ValidatelogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 定时任务动态读取配置文件
 * 如果配置内容不在application.yml或application.properties，则需要在类上注解
 *
 * @PropertySource(value = "classpath:task.properties",encoding = "UTF-8")
 * @Scheduled(cron="${Task_expireOTCAdsState_cron}") 添加定时任务
 * @Scheduled(cron = "0/5 * * * * ?")
 * 或直接指定时间间隔，例如：5秒
 * @Scheduled(fixedRate=5000) 动态设置定时任务时间, 可以实现 SchedulingConfigurer 接口
 */

// 主要用于标记配置类,兼备 Component 的效果
@Component
// 开启定时任务
@EnableScheduling
public class LogCleanTask implements SchedulingConfigurer {

    private final static Logger logger = LoggerFactory.getLogger(LogCleanTask.class);

    @Autowired
    private IworkConfig iworkConfig;
    @Autowired
    private RunLogService runLogService;
    @Autowired
    private ValidatelogService validatelogService;

    // 默认清理日志定时任务 cron 表达式,配置文件没有配置时生效,:每天凌晨0点执行一次的写法
    public static String DEFAULT_CORN = "0 0 0 */1 * ?";

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Runnable task = () -> {
            //定时清理四张日志表
            executeCleanTask("runlog_record", () -> runLogService.cleanRunlogRecord(10 * 10000));  // 只保留最近10w条
            executeCleanTask("runlog_detail", () -> runLogService.cleanRunlogDetail(100 * 10000));  // 只保留最近100w条
            executeCleanTask("validatelog_record", () -> validatelogService.cleanValidatelogRecord(10 * 10000));  // 只保留最近10w条
            executeCleanTask("validatelog_detail", () -> validatelogService.cleanValidatelogDetail(100 * 10000));  // 只保留最近100w条
        };

        Trigger trigger = triggerContext -> {
            Date date;
            if (iworkConfig.getCleanRunLogSecond() != null) {
                PeriodicTrigger periodicTrigger = new PeriodicTrigger(iworkConfig.getCleanRunLogSecond(), TimeUnit.SECONDS);
                date = periodicTrigger.nextExecutionTime(triggerContext);
            } else {
                //任务触发，可修改任务的执行周期
                CronTrigger cronTrigger = new CronTrigger(DEFAULT_CORN);
                date = cronTrigger.nextExecutionTime(triggerContext);
            }
            return date;
        };

        taskRegistrar.addTriggerTask(task, trigger);
    }

    public void executeCleanTask(String tableName, Supplier<Long> supplier) {
        logger.info("开始清理日志表 " + tableName);
        long delCount = supplier.get();
        logger.info(String.format("实际清理日志表 %s %d 条记录", tableName, delCount));
    }
}
