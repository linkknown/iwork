package com.linkknown.iwork.core.run;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.core.WorkStepFactory;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.entity.Runlog;
import com.linkknown.iwork.entity.Work;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Data
@Accessors(chain = true)
public class Runner {

    Consumer<Runlog.RunlogRecord> runlogRecordConsumer;
    Consumer<List<Runlog.RunlogDetail>> runlogDetailConsumer;

    // dispatcher 为父流程或者调用者传递下来的参数
    public Receiver runWork(Work work, Dispatcher dispatcher) {
        Receiver receiver = null;

        // 缓冲日志写入对象
        CacheLoggerWriter loggerWriter = this.initCacheLoggerWriter(dispatcher);

        WorkCache workCache = CacheManager.getInstance().getWorkCache(Integer.parseInt(work.getAppId()), work.getId());
        String trackingId = "";
        long startTime = System.currentTimeMillis();
        try {
            // 为当前流程创建新的 trackingId, 前提条件 cacheContext.Work 一定存在
            trackingId = this.createNewTrackingIdForWork(dispatcher);

            initRunlogRecord(work, trackingId);

            if (dispatcher != null && !dispatcher.isExistParentWork() && !StringUtils.equals(work.getWorkType(), "filters")) {
                // 记录继承下来的日志: 如前置 filterTrackingIds 信息
                this.recordExtendLog(dispatcher, loggerWriter, trackingId);
            }

            // 记录日志: 标记流程执行开始和结束
            this.recordStartAndEndWorkLog(trackingId, loggerWriter, workCache,"start");

            // 初始化数据中心
            DataStore initDataStore = DataStore.initDataStore(trackingId, loggerWriter, workCache, dispatcher, null);

            receiver = new BlockStepOrdersRunner()
                    .setParentStepId(Constants.PARENT_STEP_ID_FOR_START_END)
                    .setTrackingId(trackingId)
                    .setWorkCache(workCache)
                    .setLoggerWriter(loggerWriter)
                    .setStore(initDataStore)
                    .setDispatcher(dispatcher)            // dispatcher 是全流程共享的
                    .setRunOneStep(this::runOneStep)
                    .run();
        } catch (IWorkException e) {
            e.printStackTrace();
        } finally {
            this.recordStartAndEndWorkLog(trackingId, loggerWriter, workCache,"end");
            long endTime = System.currentTimeMillis();
            loggerWriter.recordCostTimeLog("execute work", "####", trackingId, endTime - startTime);
            loggerWriter.close();
        }
        if (receiver != null && StringUtils.isNotEmpty(trackingId)) {
            receiver.setTrackingId(trackingId);
        }
        return receiver;
    }

    private CacheLoggerWriter initCacheLoggerWriter(Dispatcher dispatcher) {
        CacheLoggerWriter loggerWriter = new CacheLoggerWriter().setRunlogDetailConsumer(this.runlogDetailConsumer);
        // 调度者不为空时代表有父级流程
        if (dispatcher != null && dispatcher.getTmpDataMap() != null && dispatcher.getTmpDataMap().get("logwriter") != null) {
            loggerWriter = (CacheLoggerWriter) dispatcher.getTmpDataMap().get("logwriter");
        }
        return loggerWriter;
    }

    // 执行单个 BlockStep
    private Receiver runOneStep(BlockStepOrdersRunner.RunOneStepArgs args) throws IWorkException {
        Receiver receiver = null;
        long startTime = System.currentTimeMillis();
        try {
            this.recordStartAndEndStepLog(args, "start");

            // 由工厂代为执行步骤
            WorkStepFactory factory = new WorkStepFactory();
            factory.setWorkStep(args.getBlockStep().getStep())
                    .setDispatcher(args.getDispatcher())
                    .setReceiver(receiver)
                    .setAppId(args.getAppId())
                    .setBlockStep(args.getBlockStep())
                    .setDataStore(args.getDataStore())
                    .setLoggerWriter(args.getLoggerWriter())
                    .setRunOneStep(this::runOneStep)
                    .setRunWorkSub(this::runWork)
    //                    WorkSubRunFunc:   RunOneWork,
                    .setWorkCache(args.getWorkCache());
            factory.execute(args.getTrackingId());
            // factory 节点如果代理的是 work_end 节点,则传递 Receiver 出去
            return factory.getReceiver();
        } finally {
            long endTime = System.currentTimeMillis();
            // 统计耗费时间
            args.getLoggerWriter().recordCostTimeLog(args.getBlockStep().getStep().getWorkStepName(), args.getBlockStep().getStep().getWorkStepName(), args.getTrackingId(), endTime - startTime);
            // 记录步骤开始执行和结束执行日志
            this.recordStartAndEndStepLog(args, "end");
        }
    }

    // 记录步骤开始执行和结束执行日志
    private void recordStartAndEndStepLog(BlockStepOrdersRunner.RunOneStepArgs args, String pattern) {
        String logStr = String.format("%s execute blockStep: >>>>>>>>>> <span style='color:blue;font-weight:bold;'> [[ %s ]] </span> <<<<<<<<<<",
                pattern, args.getBlockStep().getStep().getWorkStepName());
        args.getLoggerWriter().write(args.getTrackingId(), args.getBlockStep().getStep().getWorkStepName(), Constants.LOG_LEVEL_INFO, logStr);
    }

    private void recordStartAndEndWorkLog(String trackingId, CacheLoggerWriter loggerWriter, WorkCache workCache, String pattern) {
        String msg = String.format("~~~~~~~~~~%s execute work:%s~~~~~~~~~~", pattern, workCache.getWork().getWorkName());
        loggerWriter.write(trackingId, "#####", Constants.LOG_LEVEL_INFO, msg);
    }

    // 记录前置 filterTrackingIds 信息
    private void recordExtendLog(Dispatcher dispatcher, CacheLoggerWriter loggerWriter, String trackingId) {
        String filterTrackingIds = this.getFilterTrackingIds(dispatcher);
        if (StringUtils.isNotBlank(filterTrackingIds)) {
            String msg = String.format("filter chain:%s", filterTrackingIds);
            loggerWriter.write(trackingId, "", Constants.LOG_LEVEL_INFO, msg);
        }
    }

    private String getFilterTrackingIds(Dispatcher dispatcher) {
        if (dispatcher != null) {
            HttpServletRequest request = (HttpServletRequest) dispatcher.getTmpDataMap().get(Constants.HTTP_REQUEST_OBJECT);
            // 不一定由外部触发
            if (request != null) {
                String filterTrackingIds = request.getHeader(Constants.FILTER_TRACKING_ID_STACK);
                if (StringUtils.isNotEmpty(filterTrackingIds)) {
                    return filterTrackingIds;
                }
            }
        }
        return "";
    }

    private void initRunlogRecord(Work work, String trackingId) {
        Runlog.RunlogRecord runlogRecord = new Runlog.RunlogRecord();
        runlogRecord.setAppId(work.getAppId());
        runlogRecord.setTrackingId(trackingId);
        runlogRecord.setWorkId(work.getId());
        runlogRecord.setWorkName(work.getWorkName());
        runlogRecord.setLogLevel("");
        runlogRecord.setLastUpdatedTime(new Date());

        runlogRecordConsumer.accept(runlogRecord);
    }

    // 获取当前 work 需要的 trakingId
    private String createNewTrackingIdForWork(Dispatcher dispatcher) {
        // 生成当前流程的 trackingId
        String trackingId = UUID.randomUUID().toString();
        // 调度者不为空时代表有父级流程
        if (dispatcher != null && StringUtils.isNotBlank(dispatcher.getTrackingId())) {
            // 拼接父流程的 trackingId 信息,作为链式 trackingId
            // 同时优化 trackingId,防止递归调用时 trackingId 过长
            trackingId = this.optimizeTrackingId(dispatcher.getTrackingId(), trackingId);
        }
        return trackingId;
    }

    // 对 trakingId 进行优化,避免过长的 trackingId
    private String optimizeTrackingId(String pTrackingId, String trackingId) {
        if (StringUtils.countMatches(pTrackingId, ".") <= 1) {
            return String.format("%s.%s", pTrackingId, trackingId);
        }
        StringBuilder sb = new StringBuilder();
        // a.~.b.c
        sb.append(StringUtils.substring(pTrackingId, 0, StringUtils.indexOf(pTrackingId, "."))) // 顶级 trackingId
                .append(".")
                .append("~")     // 过渡级 trackingId
                .append(".")
                .append(StringUtils.substring(pTrackingId, StringUtils.lastIndexOf(pTrackingId, ".") + 1))   // 父级 trackingId
                .append(".")
                .append(trackingId);        // 当前级 trackingId
        trackingId = sb.toString();
        return trackingId;
    }
}
