package com.linkknown.iwork.quartz;

import com.linkknown.iwork.core.Memory;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.core.run.Runner;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.service.RunLogService;
import com.linkknown.iwork.util.ApplicationContextUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledJobExecutor implements Job {

    private RunLogService runLogService = ApplicationContextUtil.getBean(RunLogService.class);

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobExecutor.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();

        Work work = (Work) jobDataMap.get("work");
        logger.info("定时执行任务[开始]：" + work.getWorkName());

        WorkCache workCache = Memory.getWorkCacheByNameFromMemory(Integer.valueOf(work.getAppId()), work.getWorkName());

        Receiver receiver = new Runner()
                .setRunlogRecordConsumer(runlogRecord -> runLogService.insertRunlogRecord(runlogRecord))
                .setRunlogDetailConsumer(runlogDetails -> runLogService.insertMultiRunlogDetail(runlogDetails))
                .runWork(workCache.getWork(), new Dispatcher().setTmpDataMap(null));

        logger.info("定时执行任务[结束]：" + work.getWorkName() + receiver.toString());
    }
}
