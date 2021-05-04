package com.linkknown.iwork.mapper;

import com.linkknown.iwork.entity.OperateLog;
import org.apache.ibatis.annotations.Param;

public interface OperateLogMapper {

    void insertOperateLog(@Param("operateLog") OperateLog operateLog);
}
