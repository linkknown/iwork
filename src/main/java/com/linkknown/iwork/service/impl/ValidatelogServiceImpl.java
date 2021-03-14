package com.linkknown.iwork.service.impl;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.dao.ValidatelogMapper;
import com.linkknown.iwork.dao.WorkStepMapper;
import com.linkknown.iwork.entity.Validatelog;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.ValidatelogService;
import com.linkknown.iwork.service.WorkStepService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class ValidatelogServiceImpl implements ValidatelogService {

    @Autowired
    private ValidatelogMapper validatelogMapper;

    @Override
    public void insertMultiValidatelogDetail(List<Validatelog.ValidatelogDetail> details) {
        if (!CollectionUtils.isEmpty(details)) {
            validatelogMapper.insertMultiValidatelogDetail(details);
        }
    }

    @Override
    public void insertValidatelogRecord(Validatelog.ValidatelogRecord record) {
        validatelogMapper.insertValidatelogRecord(record);
    }

    @Override
    public List<Validatelog.ValidatelogDetail> queryLastValidatelogDetail(int workId) {
        return validatelogMapper.queryLastValidatelogDetail(workId);
    }
}
