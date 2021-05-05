package com.linkknown.iwork.mapper;

import com.linkknown.iwork.entity.OperateLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OperateLogMapper {

    void insertOperateLog(@Param("operateLog") OperateLog operateLog);

    List<OperateLog> queryPageOperateLogList(@Param("map") Map<String,Object> map, int page, int offset);
}
