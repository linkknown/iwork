package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.dao.RunLogMapper;
import com.linkknown.iwork.entity.Runlog;
import com.linkknown.iwork.service.RunLogService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class RunLogServiceImpl implements RunLogService {

    // maximumPoolSize 设置为 200,拒绝策略为 AbortPolic 策略,直接抛出异常
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(100, 200, 10000,
            TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    @Autowired
    private RunLogMapper runLogMapper;

    @Override
    public int queryRunlogRecordCount(int workId, String logLevel) {
        return runLogMapper.queryRunlogRecordCount(workId, logLevel);
    }

    @Override
    public Map<Integer, Map<String, Integer>> getRunlogRecordCount(List<Integer> workIds) {
        Map<Integer, Map<String, Integer>> m = new ConcurrentHashMap<>();

        CountDownLatch countDownLatch = new CountDownLatch(workIds.size());
        for (int workId : workIds) {

            pool.execute(() -> {
                try {
                    int errorCount = queryRunlogRecordCount(workId, "ERROR");
                    int allCount = queryRunlogRecordCount(workId, "");

                    HashMap<String, Integer> runLogRecordCountMap = new HashMap<>();
                    runLogRecordCountMap.put("errorCount", errorCount);
                    runLogRecordCountMap.put("allCount", allCount);
                    m.put(workId, runLogRecordCountMap);
                } finally {
                    countDownLatch.countDown();
                }
            });

        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return m;
    }

    @Override
    public void insertRunlogRecord(Runlog.RunlogRecord runlogRecord) {
        runLogMapper.insertRunlogRecord(runlogRecord);
    }

    @Override
    public void insertMultiRunlogDetail(List<Runlog.RunlogDetail> runlogDetails) {
        runLogMapper.insertMultiRunlogDetail(runlogDetails);
    }

    @Override
    public PageInfo<Runlog.RunlogRecord> queryRunlogRecord(Map<String, Object> map, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<Runlog.RunlogRecord> runlogRecords = runLogMapper.queryRunlogRecord(map, page, offset);
        PageInfo<Runlog.RunlogRecord> pageInfo = new PageInfo<>(runlogRecords);
        return pageInfo;
    }

    @Override
    public Runlog.RunlogRecord queryRunlogRecordWithTracking(String trackingId) {
        return runLogMapper.queryRunlogRecordWithTracking(trackingId);
    }

    @Override
    public List<Runlog.RunlogDetail> queryLastRunlogDetail(String trackingId) {
        return runLogMapper.queryLastRunlogDetail(trackingId);
    }
}
