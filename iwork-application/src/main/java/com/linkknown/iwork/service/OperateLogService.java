package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.OperateLog;

import java.util.Map;

public interface OperateLogService {

    void insertOperateLog(OperateLog operateLog);

    PageInfo<OperateLog> queryPageOperateLogList(Map<String,Object> map, int page, int offset);
}
