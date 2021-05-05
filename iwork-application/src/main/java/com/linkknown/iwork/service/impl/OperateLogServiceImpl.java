package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.OperateLog;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.mapper.OperateLogMapper;
import com.linkknown.iwork.mapper.SqlMigrateMapper;
import com.linkknown.iwork.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OperateLogServiceImpl implements OperateLogService {


    @Autowired
    private OperateLogMapper operateLogMapper;


    @Override
    public void insertOperateLog(OperateLog operateLog) {
        operateLogMapper.insertOperateLog(operateLog);
    }

    @Override
    public PageInfo<OperateLog> queryPageOperateLogList(Map<String, Object> map, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<OperateLog> operateLogs = operateLogMapper.queryPageOperateLogList(map, page, offset);
        PageInfo<OperateLog> pageInfo = new PageInfo<>(operateLogs);
        return pageInfo;
    }
}
