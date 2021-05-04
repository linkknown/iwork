package com.linkknown.iwork.service.impl;

import com.linkknown.iwork.entity.OperateLog;
import com.linkknown.iwork.mapper.OperateLogMapper;
import com.linkknown.iwork.mapper.SqlMigrateMapper;
import com.linkknown.iwork.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperateLogServiceImpl implements OperateLogService {


    @Autowired
    private OperateLogMapper operateLogMapper;


    @Override
    public void insertOperateLog(OperateLog operateLog) {
        operateLogMapper.insertOperateLog(operateLog);
    }
}
