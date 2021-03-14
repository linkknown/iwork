package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.Validatelog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ValidatelogMapper {

    void insertMultiValidatelogDetail(List<Validatelog.ValidatelogDetail> details);

    void insertValidatelogRecord(@Param("record") Validatelog.ValidatelogRecord record);

    List<Validatelog.ValidatelogDetail> queryLastValidatelogDetail(int workId);
}
