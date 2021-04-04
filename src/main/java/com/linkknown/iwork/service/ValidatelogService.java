package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.entity.Validatelog;

import java.util.List;
import java.util.Map;

public interface ValidatelogService {

    void insertMultiValidatelogDetail(List<Validatelog.ValidatelogDetail> details);

    void insertValidatelogRecord(Validatelog.ValidatelogRecord record);

    List<Validatelog.ValidatelogDetail> queryLastValidatelogDetail(int workId);

    long cleanValidatelogDetail(int keepCount);

    long cleanValidatelogRecord(int keepCount);
}
