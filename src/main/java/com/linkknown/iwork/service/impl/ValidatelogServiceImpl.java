package com.linkknown.iwork.service.impl;

import com.linkknown.iwork.mapper.ValidatelogMapper;
import com.linkknown.iwork.entity.Validatelog;
import com.linkknown.iwork.service.ValidatelogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    @Override
    public long cleanValidatelogDetail(int keepCount) {
        return validatelogMapper.cleanValidatelogDetail(keepCount);
    }

    @Override
    public long cleanValidatelogRecord(int keepCount) {
        return validatelogMapper.cleanValidatelogRecord(keepCount);
    }
}
