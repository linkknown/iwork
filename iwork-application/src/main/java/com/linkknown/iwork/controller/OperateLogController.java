package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.common.adapter.PageAdapter;
import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.entity.OperateLog;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志
 */
@RestController
@RequestMapping("/api/iwork")
public class OperateLogController {

    @Autowired
    private OperateLogService operateLogService;

    @RequestMapping("/filterPageOperateLog")
    public Object filterPageOperateLog(@RequestParam(defaultValue = "-1") int app_id,
                                     @RequestParam(defaultValue = "10") int offset,        // 每页记录数
                                     @RequestParam(defaultValue = "1") int current_page,   // 当前页
                                     @RequestParam(defaultValue = "") String search) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("search", search);

        PageInfo<OperateLog> pageInfo = operateLogService.queryPageOperateLogList(map, current_page, offset);

        resultMap.put("status", "SUCCESS");
        resultMap.put("operateLogs", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
        return resultMap;
    }

}
