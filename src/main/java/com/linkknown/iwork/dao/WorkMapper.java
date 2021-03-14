package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.Work;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface WorkMapper {

    List<Work> queryWork(@Param("map") Map<String,Object> map, int page, int offset);

    Work queryWorkById(@Param("appId") int appId, @Param("workId") int workId);

    Work queryWorkByName(@Param("appId") int appId, @Param("workName") String workName);

    void insertOrUpdateWork(@Param("work") Work work);

    void deleteWorkById(int workId);

    List<Work> queryWorksByWorkType(@Param("appId") int appId, @Param("workType") String workType);

    List<Work> queryAllWorks(int appId);
}
