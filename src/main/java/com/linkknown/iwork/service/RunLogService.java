package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.Runlog;
import com.linkknown.iwork.entity.Work;

import java.util.List;
import java.util.Map;

public interface RunLogService {

    int queryRunlogRecordCount(int workId, String logLevel);

    Map<Integer, Map<String, Integer>> getRunlogRecordCount(List<Integer> workIds);

    void insertRunlogRecord(Runlog.RunlogRecord runlogRecord);

    void insertMultiRunlogDetail(List<Runlog.RunlogDetail> runlogDetails);

    PageInfo<Runlog.RunlogRecord> queryRunlogRecord(Map<String,Object> map, int page, int offset);

    Runlog.RunlogRecord queryRunlogRecordWithTracking(String trackingId);

    List<Runlog.RunlogDetail> queryLastRunlogDetail(String trackingId);

    /**
     * 清理日志表
     * @param keepCount 保留最近条数
     */
    long cleanRunlogDetail(int keepCount);

    long cleanRunlogRecord(int keepCount);
}
