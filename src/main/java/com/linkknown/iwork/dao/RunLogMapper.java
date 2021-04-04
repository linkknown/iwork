package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.Runlog;
import com.linkknown.iwork.entity.Work;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface RunLogMapper {

    int queryRunlogRecordCount(@Param("workId") int workId, @Param("logLevel") String logLevel);

    void insertRunlogRecord(@Param("runlogRecord") Runlog.RunlogRecord runlogRecord);

    void insertMultiRunlogDetail(@Param("list") List<Runlog.RunlogDetail> runlogDetails);

    List<Runlog.RunlogRecord> queryRunlogRecord(@Param("map") Map<String,Object> map, int page, int offset);

    Runlog.RunlogRecord queryRunlogRecordWithTracking(String trackingId);

    List<Runlog.RunlogDetail> queryLastRunlogDetail(String trackingId);

    long cleanRunlogDetail(int keepCount);

    long cleanRunlogRecord(int keepCount);
}
