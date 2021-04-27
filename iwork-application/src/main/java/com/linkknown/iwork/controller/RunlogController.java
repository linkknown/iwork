package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.common.adapter.PageAdapter;
import com.linkknown.iwork.entity.Runlog;
import com.linkknown.iwork.service.RunLogService;
import com.linkknown.iwork.util.HightLightUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class RunlogController {

    @Autowired
    private RunLogService runLogService;

    @RequestMapping("/filterPageLogRecord")
    public Object filterPageLogRecord(@RequestParam(defaultValue = "-1") int app_id,
                                      @RequestParam(defaultValue = "-1") int work_id,
                                     @RequestParam(defaultValue = "10") int offset,        // 每页记录数
                                     @RequestParam(defaultValue = "1") int current_page,   // 当前页
                                     @RequestParam(defaultValue = "") String logLevel) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("appId", app_id);
        map.put("workId", work_id);
        map.put("logLevel", logLevel);

        PageInfo<Runlog.RunlogRecord> pageInfo = runLogService.queryRunlogRecord(map, current_page, offset);

        resultMap.put("status", "SUCCESS");
        resultMap.put("runLogRecords", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
        return resultMap;
    }

    @RequestMapping("/getLastRunlogDetail")
    public Object getLastRunlogDetail(@RequestParam(defaultValue = "") String tracking_id) {
        Map<String, Object> resultMap = new HashMap<>();

        Runlog.RunlogRecord runLogRecord = runLogService.queryRunlogRecordWithTracking(tracking_id);
        List<Runlog.RunlogDetail> runLogDetails = runLogService.queryLastRunlogDetail(tracking_id);
        runLogDetails.stream().forEach(runlogDetail -> runlogDetail.setDetail(HightLightUtil.hightLight(runlogDetail.getDetail())));

        resultMap.put("status", "SUCCESS");
        resultMap.put("runLogRecord", runLogRecord);
        resultMap.put("runLogDetails", runLogDetails);
        return resultMap;
    }
}
